package fan.yumetsuki.yumerpg.ecs

interface ECSWorld {

    suspend fun onTick()

}

class CommonRpgWorld : ECSWorld {

    override suspend fun onTick() {
        TODO("Not yet implemented")
    }

}