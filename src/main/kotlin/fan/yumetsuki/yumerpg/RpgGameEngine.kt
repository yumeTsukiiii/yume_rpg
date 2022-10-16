package fan.yumetsuki.yumerpg

import fan.yumetsuki.yumerpg.game.*

object RpgGameEngine {

    suspend fun createAccount(
        accountCreator: RpgAccountCreator
    ): RpgAccount = accountCreator.create()

    suspend fun login(
        accountValidator: RpgAccountValidator
    ): RpgAccount = accountValidator.validate()

    suspend fun startGame(
        gameStarter: GameStarter
    ): RpgGame = gameStarter.start()

}