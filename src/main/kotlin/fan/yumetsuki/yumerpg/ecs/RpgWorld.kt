package fan.yumetsuki.yumerpg.ecs

interface RpgWorld {

    suspend fun onTick()

}

class CommonRpgWorld : RpgWorld {

    override suspend fun onTick() {
        TODO("Not yet implemented")
    }

}