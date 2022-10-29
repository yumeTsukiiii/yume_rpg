package fan.yumetsuki.yumerpg.ecs

interface RpgEntity {

    val name: String

    suspend fun components(): List<RpgComponent>

}