package fan.yumetsuki.yumerpg.game

interface RpgGame {

    suspend fun join(account: RpgAccount): RpgPlayer

    suspend fun isRunning(): Boolean

    suspend fun stop()

    suspend fun exit(account: RpgAccount)

    suspend fun save(account: RpgAccount)

    suspend fun getPlayerOrNull(account: RpgAccount): RpgPlayer?

    suspend fun players(): List<RpgPlayer>

}

suspend fun RpgGame.getPlayer(account: RpgAccount): RpgPlayer = getPlayerOrNull(account)!!

interface GameStarter {

    suspend fun start(): RpgGame

}