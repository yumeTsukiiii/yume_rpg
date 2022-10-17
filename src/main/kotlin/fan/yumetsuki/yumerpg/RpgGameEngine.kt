package fan.yumetsuki.yumerpg

import fan.yumetsuki.yumerpg.game.*

object RpgGameEngine {

    suspend fun startGame(
        gameStarter: GameStarter
    ): RpgGame = gameStarter.start()

}