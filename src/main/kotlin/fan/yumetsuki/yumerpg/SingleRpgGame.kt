package fan.yumetsuki.yumerpg

import fan.yumetsuki.yumerpg.game.GameStarter
import fan.yumetsuki.yumerpg.game.RpgAccount
import fan.yumetsuki.yumerpg.game.RpgGame
import fan.yumetsuki.yumerpg.game.RpgPlayer

private object EmptyRpgGame: RpgGame {

    override suspend fun join(account: RpgAccount): RpgPlayer {
        error("EmptyRpgGame")
    }

    override suspend fun isRunning(): Boolean {
        return false
    }

    override suspend fun stop() = Unit

    override suspend fun exit(account: RpgAccount) = Unit

    override suspend fun save(account: RpgAccount) = Unit

    override suspend fun getPlayerOrNull(account: RpgAccount): RpgPlayer? = null

    override suspend fun players(): List<RpgPlayer> {
        error("EmptyRpgGame")
    }

}

/**
 * 游戏单例，用于创建游戏的统一入口
 * @author yumetsuki
 */
@Suppress("MemberVisibilityCanBePrivate")
object SingleRpgGame : RpgGame {

    private lateinit var innerGame: RpgGame

    suspend fun start(starter: GameStarter) {
        if (!isRunning()) {
            innerGame = starter.start()
        }
    }

    suspend fun restart(starter: GameStarter) {
        if (isRunning()) {
            innerGame.stop()
            innerGame = starter.start()
        }
    }

    override suspend fun join(account: RpgAccount): RpgPlayer {
        if (!isRunning()) {
            error("游戏未运行，无法加入...")
        }
        return innerGame.join(account)
    }

    override suspend fun stop() {
        if (!isRunning()) {
            return
        }
        return innerGame.stop()
    }

    override suspend fun exit(account: RpgAccount) {
        if (!isRunning()) {
            return
        }
        return innerGame.exit(account)
    }

    override suspend fun save(account: RpgAccount) {
        if (!isRunning()) {
            return
        }
        return innerGame.save(account)
    }

    override suspend fun getPlayerOrNull(account: RpgAccount): RpgPlayer? {
        if (!isRunning()) {
            return null
        }
        return innerGame.getPlayerOrNull(account)
    }

    override suspend fun players(): List<RpgPlayer> {
        if (!isRunning()) {
            return emptyList()
        }
        return innerGame.players()
    }

    override suspend fun isRunning(): Boolean {
        return this::innerGame.isInitialized && innerGame.isRunning()
    }
}