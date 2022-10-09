package fan.yumetsuki.yumerpg.core.game.model

import fan.yumetsuki.yumerpg.core.model.*
import fan.yumetsuki.yumerpg.core.utils.RangeProperty

/**
 * 创建一个角色，一个角色是拥有[CharacterAbility]能力的对象
 * 角色可以是敌人，可以是伙伴，也可以是中立（NPC）
 * @return [RpgModel]角色对象
 * @author yumetsuki
 */
fun character(
    // 角色名
    name: String,
    characterType: CharacterAbility.CharacterType,
    abilities: List<RpgAbility<*, *, *, *>>
) : RpgModel {

    return CommonRpgModel(
        meta = mapMeta(
            "name" to name
        ),
        abilities = listOf(
            CharacterAbility(characterType),
            *abilities.filter {
                it !is CharacterAbility
            }.toTypedArray()
        )
    )

}

/**
 * 判断对象是否为角色
 * @author yumetsuki
 */
fun RpgModel.isCharacter(): Boolean = getAbility<CharacterAbility, _, _, _, _>() != null

/**
 * 判断对象是否为伙伴
 * @author yumetsuki
 */
fun RpgModel.isParty(): Boolean = getAbility<CharacterAbility, _, _, _, _>()?.value == CharacterAbility.CharacterType.Party

/**
 * 判断对象是否为敌人
 * @author yumetsuki
 */
fun RpgModel.isEnemy(): Boolean = getAbility<CharacterAbility, _, _, _, _>()?.value == CharacterAbility.CharacterType.Enemy

/**
 * 判断对象是否为 Npc
 * @author yumetsuki
 */
fun RpgModel.isNpc(): Boolean = getAbility<CharacterAbility, _, _, _, _>()?.value == CharacterAbility.CharacterType.Npc

/**
 * 角色拥有的能力，可以标识自己的特征，是否为伙伴/敌人
 * @author yumetsuki
 */
class CharacterAbility(
    override var value: CharacterType,
    override val name: String = "Character",
    override val alias: String? = "角色"
): PropertyAbility<CharacterAbility.CharacterType, RpgModel> {

    /**
     * 角色类型，用于标识角色的特征
     */
    class CharacterType(
        val type: String
    ) {
        companion object {
            /**
             * 伙伴
             */
            val Party = CharacterType("Party")

            /**
             * 敌人
             */
            val Enemy = CharacterType("Enemy")

            /**
             * Npc
             */
            val Npc = CharacterType("Npc")

            /**
             * 空角色，属于角色定义的范畴，它未在当前游戏中起到任何实际作用，仅仅是一个可供实例化的参考
             * [CharacterType]为[CharacterType.Empty]的对象不应在游戏对象中出现，仅作为角色类别定义
             */
            val Empty = CharacterType("Empty")
        }

    }

}

/* 下面实现 PropertyAbility 的能力，为角色可以拥有的基本属性 */

/**
 * 等级
 * @author yumetsuki
 */
class LevelAbility(override var value: Int, override val name: String = "Level", override val alias: String? = "等级") : PropertyAbility<Int, RpgModel>

/**
 * Hp 值
 * @author yumetsuki
 */
class HpAbility(
    value: RangeProperty<Int>,
    override val name: String = "Hp"
) : PropertyAbility<Int, RpgModel> {

    override var value: Int by value

}

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