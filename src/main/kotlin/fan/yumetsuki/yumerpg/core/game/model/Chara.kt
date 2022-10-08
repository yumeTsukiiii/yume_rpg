package fan.yumetsuki.yumerpg.core.game.model

import fan.yumetsuki.yumerpg.core.model.PropertyAbility
import fan.yumetsuki.yumerpg.core.model.RpgModel

/* 下面实现 PropertyAbility 的能力，为角色拥有的基本属性 */

/**
 * 等级
 * @author yumetsuki
 */
class LevelAbility(override var value: Int, override val name: String = "Level", override val alias: String? = "等级") : PropertyAbility<Int, RpgModel>

/**
 * Hp 值
 * @author yumetsuki
 */
class HpAbility(override var value: Int, override val name: String = "Hp") : PropertyAbility<Int, RpgModel>

/**
 * Mp 值
 * @author yumetsuki
 */
class MpAbility(override var value: Int, override val name: String = "Mp") : PropertyAbility<Int, RpgModel>

/**
 * 力量值
 * @author yumetsuki
 */
class PowerAbility(override var value: Int, override val name: String = "Power", override val alias: String? = "力量值") : PropertyAbility<Int, RpgModel>

/**
 * 魔力值
 * @author yumetsuki
 */
class MagicAbility(override var value: Int, override val name: String = "Magic", override val alias: String? = "魔力值") : PropertyAbility<Int, RpgModel>

/**
 * 金钱数
 * @author yumetsuki
 */
class MoneyAbility(override var value: Long, override val name: String = "Gold", override val alias: String? = "金钱") : PropertyAbility<Long, RpgModel>

/* 人物持有的物品 */

/**
 * 持有的道具种类
 * @author yumetsuki
 */
class ItemsAbility(
    override var value: List<RpgModel>,
    override val name: String = "Items",
    override val alias: String? = "道具"
) : PropertyAbility<List<RpgModel>, RpgModel>

/**
 * 持有的装备种类
 * @author yumetsuki
 */
class WeaponsAbility(
    override var value: List<RpgModel>,
    override val name: String = "Weapons",
    override val alias: String? = "装备"
) : PropertyAbility<List<RpgModel>, RpgModel>

/**
 * 持有的技能，由职业和装备决定，可独立配置额外技能
 * @author yumetsuki
 */
class SkillsAbility(
    private val extraSkills: List<RpgModel>,
    override val name: String = "Skills",
    override val alias: String? = "技能"
) : PropertyAbility<List<RpgModel>, RpgModel> {

    init {
        // TODO 检查 extraSkills 全部都为技能型 Model
    }

    override var value: List<RpgModel>
        get() = TODO("Not yet implemented")
        set(_) = error("Cannot change skills")

}