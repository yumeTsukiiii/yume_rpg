package fan.yumetsuki.yumerpg.ecs

interface ECSComponent {

    val identify: String
        get() = this::class.simpleName?: error("RpgComponent 必须具有唯一标识")

}