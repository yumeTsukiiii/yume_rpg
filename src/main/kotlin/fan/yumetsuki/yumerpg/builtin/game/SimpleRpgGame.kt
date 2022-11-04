package fan.yumetsuki.yumerpg.builtin.game

import fan.yumetsuki.yumerpg.SingleRpgGame
import fan.yumetsuki.yumerpg.builtin.RpgEntity
import fan.yumetsuki.yumerpg.builtin.rpgobject.PropertyComponentConstructor
import fan.yumetsuki.yumerpg.builtin.RpgSystem
import fan.yumetsuki.yumerpg.builtin.rpgobject.PropertyChangeComponentConstructor
import fan.yumetsuki.yumerpg.builtin.rpgobject.PropertyChangeSystemConstructor
import fan.yumetsuki.yumerpg.ecs.ECSWorld
import fan.yumetsuki.yumerpg.ecs.SimpleECSWorld
import fan.yumetsuki.yumerpg.game.*
import fan.yumetsuki.yumerpg.serialization.*
import fan.yumetsuki.yumerpg.serialization.protocol.JsonByteElementProtocol
import fan.yumetsuki.yumerpg.serialization.protocol.JsonByteObjectProtocol
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

val globalRpgObjectConstructors: List<RpgObjectConstructor> = listOf(
    PropertyComponentConstructor(),
    PropertyChangeComponentConstructor(),
    PropertyChangeSystemConstructor()
)

/**
 * 给 [SimpleRpgGame] 使用的游戏配置
 * @author yumetsuki
 */
class SimpleGameStarterConfig(
    /**
     * 可配置的 Byte [RpgElement] 解析协议
     */
    val rpgElementProtocol: RpgElementProtocol<ByteArray> = JsonByteElementProtocol,
    /**
     * 可配置的 Byte [RpgObject] 解析协议
     */
    val rpgObjectProtocol: RpgObjectProtocol<ByteArray> = JsonByteObjectProtocol,
    /**
     * [RpgElement] 配置文件集合
     */
    val rpgElementFiles: List<File>,
    /**
     * [RpgSystem] 配置文件集合
     */
    val rpgSystemFiles: List<File>,
    /**
     * [RpgObjectConstructor] 配置
     */
    val rpgObjConstructorCenter: RpgObjConstructorCenter,
    /**
     * 默认的初始化存档，玩家第一次进入游戏时默认初始化的数据
     */
    val defaultDataFile: File,
    /**
     * 读取初始化存档时，进行的可选初始化逻辑
     */
    val rpgObjectInitializer: suspend RpgObject.() -> Unit
) {

    class Builder {

        var rpgElementFiles: List<File> = listOf(File(DEFAULT_ELEMENT_FILE))

        var rpgSystemFiles: List<File> = listOf(File(DEFAULT_SYSTEM_FILE))

        var defaultDataFile: File = File(DEFAULT_DATA_FILE)

        var rpgElementProtocol: RpgElementProtocol<ByteArray> = JsonByteElementProtocol

        var rpgObjectProtocol: RpgObjectProtocol<ByteArray> = JsonByteObjectProtocol

        private val rpgObjConstructorCenter = CommonRpgObjConstructorCenter()

        private var rpgObjectInitializer: suspend RpgObject.() -> Unit = {}

        init {
            globalRpgObjectConstructors.forEach {
                rpgObjConstructorCenter.registerConstructor(it)
            }
        }

        fun registerRpgObjectConstructor(vararg rpgObjectConstructor: RpgObjectConstructor) {
            rpgObjectConstructor.forEach {
                rpgObjConstructorCenter.registerConstructor(it)
            }
        }

        fun onInitRpgObject(initializer: suspend RpgObject.() -> Unit) {
            rpgObjectInitializer = initializer
        }

        fun build() : SimpleGameStarterConfig = SimpleGameStarterConfig(
            rpgElementProtocol,
            rpgObjectProtocol,
            rpgElementFiles,
            rpgSystemFiles,
            rpgObjConstructorCenter,
            defaultDataFile,
            rpgObjectInitializer
        )

    }

    companion object {
        const val DEFAULT_ELEMENT_FILE = "system/element/rpg_element.rpg"
        const val DEFAULT_DATA_FILE = "system/default_save.yumerpg"
        const val DEFAULT_SYSTEM_FILE = "system/rpg_system.yumerpg"
    }

}

/**
 * [SimpleRpgGame] 的启动器
 * @author yumetsuki
 */
class SimpleGameStarter(
    private val simpleGameStarterConfig: SimpleGameStarterConfig
): GameStarter {

    override suspend fun start(): RpgGame = simpleGameStarterConfig.run {
        val elementCenter = CommonRpgElementCenter().apply {
            withContext(Dispatchers.IO) {
                rpgElementFiles.map {
                    it.parentFile?.also { parentDir ->
                        if (!parentDir.exists()) {
                            parentDir.mkdirs()
                        }
                    }
                    rpgElementProtocol.decodeFromContent(it.readBytes())
                }
            }.forEach {
                registerElement(it)
            }
        }

        val world = SimpleECSWorld()

        SimpleRpgGame(
            // 启动 GameWorld
            world.run {
                addSystem(
                    *createSystems(elementCenter).toTypedArray()
                )
                CoroutineScope(Dispatchers.Default).apply {
                    launch(Dispatchers.IO) {
                        while (true) {
                            try {
                                onTick()
                                delay(16.0.milliseconds)
                            } catch (e: CancellationException) {
                                break
                            }
                        }
                    }
                }
            },
            world,
            elementCenter,
            rpgObjConstructorCenter,
            rpgObjectProtocol,
            defaultDataFile,
            rpgObjectInitializer
        )
    }

    private suspend fun SimpleGameStarterConfig.createSystems(rpgElementCenter: RpgElementCenter): List<RpgSystem> {
        return withContext(Dispatchers.IO) {
            rpgSystemFiles.map {
                it.parentFile?.also { parentDir ->
                    if (!parentDir.exists()) {
                        parentDir.mkdirs()
                    }
                }
                rpgObjectProtocol.decodeFromContent(
                    CommonRpgObjSerializeContext(
                        rpgElementCenter,
                        rpgObjConstructorCenter
                    ),
                    it.readBytes()
                )
            }
        }.mapNotNull {
            when(it) {
                is RpgSystem -> listOf(it)
                is RpgObjectArray -> it.filterIsInstance<RpgSystem>()
                else -> null
            }
        }.flatten()
    }
}

class SimpleRpgAccount(override val id: Long) :RpgAccount

class SimpleRpgPlayer(
    override val account: RpgAccount,
    private val data: RpgObject,
    private val game: RpgGame
): RpgPlayer {

    override suspend fun data(): RpgObject = data

    override suspend fun exit() {
        game.exit(account)
    }

    override suspend fun save() {
        game.save(account)
    }

    override suspend fun others(): List<RpgPlayer> {
        return game.players().filter {
            it.account.id != account.id
        }
    }

}

/**
 * 一个基于文件存档和命令系统的简单 [RpgGame]，支持可配置的文件存储协议
 * @author yumetsuki
 */
class SimpleRpgGame(
    private val runLoop: CoroutineScope,
    private val world: ECSWorld,
    private val rpgElementCenter: RpgElementCenter,
    private val rpgObjConstructorCenter: RpgObjConstructorCenter,
    private val rpgObjectProtocol: RpgObjectProtocol<ByteArray>,
    private val defaultDataFile: File,
    private val rpgObjectInitializer: suspend RpgObject.() -> Unit
): RpgGame {

    private val rpgObjSerializeContext: RpgObjSerializeContext = CommonRpgObjSerializeContext(
        rpgElementCenter,
        rpgObjConstructorCenter
    )

    private val playerMutex = Mutex()
    // accountId to Player
    private val players = mutableMapOf<Long, Pair<RpgAccount, RpgPlayer>>()

    override suspend fun join(account: RpgAccount): RpgPlayer {
        return SimpleRpgPlayer(
            account,
            account.getSaveFile().readBytes().takeIf {
                it.isNotEmpty()
            }?.let {
                rpgObjectProtocol.decodeFromContent(rpgObjSerializeContext, it)
            } ?: withContext(Dispatchers.IO) {
                defaultDataFile.readBytes().let { content ->
                    rpgObjectProtocol.decodeFromContent(rpgObjSerializeContext, content).also {
                        rpgObjectInitializer(it)
                    }
                }.also {
                    world.addEntity(
                        *when(it) {
                            is RpgEntity -> listOf(it)
                            is RpgObjectArray -> it.filterIsInstance<RpgEntity>()
                            else -> emptyList()
                        }.toTypedArray()
                    )
                }
            },
            this
        ).apply {
            players[account.id] = account to this
            withContext(Dispatchers.IO) {
                launch {
                    save()
                }
            }
        }
    }

    override suspend fun isRunning(): Boolean {
        return runLoop.isActive
    }

    override suspend fun stop() {
        if (runLoop.isActive) {
            runLoop.cancel()
        }
    }

    override suspend fun exit(account: RpgAccount) {
        playerMutex.withLock {
            players.remove(account.id)
        }
    }

    override suspend fun getPlayerOrNull(account: RpgAccount): RpgPlayer? = playerMutex.withLock {
        players[account.id]?.second
    }

    override suspend fun players(): List<RpgPlayer> = playerMutex.withLock {
        players.values.map { it.second }.toList()
    }

    override suspend fun save(account: RpgAccount) {
        playerMutex.withLock {
            players[account.id]?.let { (account, player) ->
                coroutineScope {
                    launch(Dispatchers.IO) {
                        account.getSaveFile().writeText(rpgObjectProtocol.encodeToContent(
                            CommonRpgObjSerializeContext(
                                rpgElementCenter, rpgObjConstructorCenter
                            ),
                            player.data()
                        ).decodeToString())
                    }
                }
            }
        }
    }

    private suspend fun RpgAccount.getSaveFile() : File = coroutineScope {
        withContext(Dispatchers.IO) {
            File("system/save/save-${id}.yumerpg").apply {
                if (!exists()) {
                    if (!parentFile.exists()) {
                        parentFile.mkdirs()
                    }
                    createNewFile()
                }
            }
        }
    }

}

suspend fun SingleRpgGame.start(initializer: SimpleGameStarterConfig.Builder.() -> Unit) : RpgGame {
    start(
        SimpleGameStarter(
            SimpleGameStarterConfig.Builder().apply(initializer).build()
        )
    )
    return this
}

suspend fun RpgGame.join(id: Long) : RpgPlayer {
    return join(SimpleRpgAccount(id))
}

suspend fun RpgGame.exit(id: Long) {
    return exit(SimpleRpgAccount(id))
}

suspend fun RpgGame.save(id: Long) {
    return save(SimpleRpgAccount(id))
}