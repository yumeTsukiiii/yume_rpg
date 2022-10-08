package fan.yumetsuki.yumerpg.core.game.builder

import fan.yumetsuki.yumerpg.core.model.RpgAbility
import fan.yumetsuki.yumerpg.core.model.RpgModel

/**
 * 游戏对象构建器，用于解析参数列表构建指定的对象
 * 可能构建出各种对象，比如[RpgModel]或者[RpgAbility]
 * @author yumetsuki
 */
interface RpgBuilder<BuildResult> {

    /**
     * 构建器的 id，用于构建系统全局管理
     */
    val id: Long

    /**
     * 构建一个[BuildResult]对象
     * @return [BuildResult] 可能是游戏中的任何对象
     */
    fun build(param: Map<String, Any>): BuildResult

}

/**
 * [RpgModel]构建器，用于解析参数列表构建指定的对象
 * @author yumetsuki
 */
interface RpgModelBuilder : RpgBuilder<RpgModel>

/**
 * [RpgAbility]构建器，用于解析参数列表构建指定的能力
 * @author yumetsuki
 */
interface RpgAbilityBuilder : RpgBuilder<RpgAbility<*, *, *, *>>