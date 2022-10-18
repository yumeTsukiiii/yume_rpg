package fan.yumetsuki.yumerpg.builtin.game

import fan.yumetsuki.yumerpg.RpgGameEngine
import fan.yumetsuki.yumerpg.builtin.constructor.PropertyAbilityConstructor
import fan.yumetsuki.yumerpg.game.*
import fan.yumetsuki.yumerpg.serialization.*
import fan.yumetsuki.yumerpg.serialization.protocol.JsonByteElementProtocol
import fan.yumetsuki.yumerpg.serialization.protocol.JsonByteObjectProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

val globalRpgObjectConstructors = listOf<RpgObjectConstructor>(
    PropertyAbilityConstructor()
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

        var rpgElementFiles: List<File> = listOf(File("system/rpg_element.yumerpg"))

        var defaultDataFile: File = File("system/default-save.yumerpg")

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
            rpgObjConstructorCenter,
            defaultDataFile,
            rpgObjectInitializer
        )

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
        SimpleRpgGame(
            CommonRpgElementCenter().apply {
                withContext(Dispatchers.IO) {
                    rpgElementFiles.map {
                        rpgElementProtocol.decodeFromContent(it.readBytes())
                    }
                }.forEach(this::registerElement)
            },
            rpgObjConstructorCenter,
            rpgObjectProtocol,
            defaultDataFile,
            rpgObjectInitializer
        )
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
            File("save/save-${id}.yumerpg").apply {
                if (!exists()) {
                    if (!parentFile.exists()) {
                        parentFile.mkdir()
                    }
                    createNewFile()
                }
            }
        }
    }

}

suspend fun RpgGameEngine.startGame(initializer: SimpleGameStarterConfig.Builder.() -> Unit) : RpgGame {
    return startGame(
        SimpleGameStarter(
            SimpleGameStarterConfig.Builder().apply(initializer).build()
        )
    )
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