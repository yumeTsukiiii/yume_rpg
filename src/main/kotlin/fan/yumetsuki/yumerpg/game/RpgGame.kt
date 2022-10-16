package fan.yumetsuki.yumerpg.game

interface RpgGame {

    suspend fun join(account: RpgAccount): RpgPlayer

    suspend fun getPlayer(account: RpgAccount): RpgPlayer

    suspend fun players(): List<RpgPlayer>

}

interface GameStarter {

    suspend fun start(): RpgGame

}

class RpgGameConfig {



}

class YumeSimpleRpgGame(

) : RpgGame {



    override suspend fun join(account: RpgAccount): RpgPlayer {
        TODO("Not yet implemented")
    }

    override suspend fun getPlayer(account: RpgAccount): RpgPlayer {
        TODO("Not yet implemented")
    }

    override suspend fun players(): List<RpgPlayer> {
        TODO("Not yet implemented")
    }


}