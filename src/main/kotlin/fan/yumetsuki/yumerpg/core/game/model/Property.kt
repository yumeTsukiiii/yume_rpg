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
fun RpgModel.isCharacter(): Boolean = getAbility<CharacterAbility>() != null

/**
 * 判断对象是否为伙伴
 * @author yumetsuki
 */
fun RpgModel.isParty(): Boolean = getAbility<CharacterAbility>()?.value == CharacterAbility.CharacterType.Party

/**
 * 判断对象是否为敌人
 * @author yumetsuki
 */
fun RpgModel.isEnemy(): Boolean = getAbility<CharacterAbility>()?.value == CharacterAbility.CharacterType.Enemy

/**
 * 判断对象是否为 Npc
 * @author yumetsuki
 */
fun RpgModel.isNpc(): Boolean = getAbility<CharacterAbility>()?.value == CharacterAbility.CharacterType.Npc

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