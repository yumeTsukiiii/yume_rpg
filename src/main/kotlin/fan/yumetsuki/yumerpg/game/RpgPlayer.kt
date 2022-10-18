package fan.yumetsuki.yumerpg.game

import fan.yumetsuki.yumerpg.serialization.RpgObject

/**
 * 游戏玩家，主要的游戏业务的操作入口
 * @author yumetsuki
 */
interface RpgPlayer {

    val account: RpgAccount

    suspend fun data(): RpgObject

    suspend fun exit()

    suspend fun save()

    suspend fun others(): List<RpgPlayer>

}