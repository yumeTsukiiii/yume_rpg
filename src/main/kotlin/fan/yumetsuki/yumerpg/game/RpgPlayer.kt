package fan.yumetsuki.yumerpg.game

import fan.yumetsuki.yumerpg.serialization.RpgObject

interface RpgPlayerCommand {

    val id: Long

    suspend fun onExecute(player: RpgPlayer)

}

interface RpgPlayer {

    val account: RpgAccount

    suspend fun executeCommand(commandId: Long)

    suspend fun data(): RpgObject

    suspend fun exit()

    suspend fun save()

    suspend fun others(): List<RpgPlayer>

}