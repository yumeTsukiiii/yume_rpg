package fan.yumetsuki.yumerpg.core.protocol

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
    fun build(buildObject: RpgBuildObject?): BuildResult

}

/**
 * 构建器构建时的对象，它封装了单个原始数据对象的协议，用来获取构建时的各种信息
 * @author yumetsuki
 */
interface RpgBuildObject {

    fun getInt(key: String): Int

    fun getDouble(key: String): Double

    fun getString(key: String): String

    fun getAbilities(): List<RpgAbility<*, *, *, *>>

}

/**
 * [RpgModel]构建器，用于解析参数列表构建指定的对象
 * @author yumetsuki
 */
typealias RpgModelBuilder = RpgBuilder<RpgModel>

/**
 * [RpgAbility]构建器，用于解析参数列表构建指定的能力
 * @author yumetsuki
 */
typealias RpgAbilityBuilder = RpgBuilder<RpgAbility<*, *, *, *>>