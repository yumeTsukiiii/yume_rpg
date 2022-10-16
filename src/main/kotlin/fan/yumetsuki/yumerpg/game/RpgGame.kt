package fan.yumetsuki.yumerpg.game

import fan.yumetsuki.yumerpg.serialization.*

interface RpgElementLoader {

    suspend fun load(): RpgElement

}

interface RpgObjectLoader {

    suspend fun load(): RpgObject

}

interface Position {



}

interface Player {

    suspend fun others(): List<Player>

    suspend fun moveTo(position: Position)

}

interface RpgGame {

    suspend fun join(id: Long): Player

    suspend fun getPlayer(id: Long): Player

    suspend fun players(): List<Player>

}

interface RpgGameConfig {

    val rpgElementLoader: RpgElementLoader

}

interface RpgGameEngine {

    suspend fun startGame(gameConfig: RpgGameConfig): RpgGame

}

class DefaultGame(
    rpgElement: RpgElement
): RpgGame {

    val rpgElementCenter: MutableRpgElementCenter = CommonRpgElementCenter()

    init {
        rpgElementCenter.registerElement(rpgElement)
    }

    override suspend fun join(id: Long): Player {
        TODO("Not yet implemented")
    }

    override suspend fun getPlayer(id: Long): Player {
        TODO("Not yet implemented")
    }

    override suspend fun players(): List<Player> {
        TODO("Not yet implemented")
    }

}

class DefaultGameEngine : RpgGameEngine {

    override suspend fun startGame(gameConfig: RpgGameConfig): RpgGame {
        val rpgElement = gameConfig.rpgElementLoader.load()
        TODO()
    }

}

suspend fun main() {

}

