package fan.yumetsuki.yumerpg.game.store

class PlayerData(
    val account: String,
    val password: String?,
    val hp: Int,
    val mp: Int,
    // 持有物品id
    val items: List<Int>,
    // 已装备物品id
    val weapons: List<Int>,
)

interface PlayerDataManager {

    suspend fun save(playerData: PlayerData)

    suspend fun load(account: String, password: String?): PlayerData

}

