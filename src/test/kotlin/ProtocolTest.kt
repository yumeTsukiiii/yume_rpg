import fan.yumetsuki.yumerpg.core.serialization.*
import fan.yumetsuki.yumerpg.core.serialization.protocol.JsonRpgElementProtocol
import fan.yumetsuki.yumerpg.core.serialization.protocol.JsonRpgObjectProtocol
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestAbility(
    override val elementId: Long,
    override val name: String,
    override var value: Int
) : PropertyAbility<Int, RpgModel>

class TestAbilityConstructor : RpgObjectConstructor {

    override val id: Long
        get() = 1

    override fun construct(context: RpgObjectConstructContext): RpgObject {
        return TestAbility(
            elementId = context.elementId,
            name = context.getStringOrNull("name")!!,
            value = context.getIntOrNull("value")!!
        )
    }

    override fun deconstruct(context: RpgObjectDeconstructContext) {
        val rpgObject = context.rpgObject<TestAbility>()
        context.deconstruct {
            put("name", rpgObject.name)
            put("value", rpgObject.value)
        }
    }

}

class TestRpgModelConstructor : RpgObjectConstructor {

    override val id: Long
        get() = 2

    override fun construct(context: RpgObjectConstructContext): RpgObject {
        return CommonRpgModel(
            elementId = context.elementId,
            meta = mapRpgMeta(
                "name" to context.getStringOrNull("name")!!
            ),
            abilities = (
                context.getRpgObjectOrNull("abilities")!! as RpgObjectArray
            ).filterIsInstance<RpgAbility<*, *, *, *>>()
        )
    }

    override fun deconstruct(context: RpgObjectDeconstructContext) {
        val rpgObject = context.rpgObject<CommonRpgModel>()

        context.deconstruct {
            put("name", rpgObject.meta().get<String>("name"))
            put("abilities", RpgObjectArray(rpgObject.abilities()))
        }
    }

}

class ProtocolTest {

    @Test
    fun testJsonProtocol() {
        val elementsContent = """
                [
                    {
                        "id": 1,
                        "constructor": 1,
                        "data": {
                            "name": "TestAbility"
                        }
                    },
                    {
                        "id": 2,
                        "constructor": 2,
                        "data": {
                            "name": "TestRpgModel",
                            "abilities": [
                                1
                            ]
                        }
                    }
                ]
            """.trimIndent()
        val dataContent = """
                [
                    {
                        "elementId": 2,
                        "data": {
                            "abilities": [
                                {
                                    "elementId": 1,
                                    "data": {
                                        "value": 1
                                    }
                                },
                                {
                                    "elementId": 1,
                                    "data": {
                                        "value": 2
                                    }
                                }
                            ]
                        }
                    }
                ]
            """.trimIndent()
        val elements = JsonRpgElementProtocol.decodeFromContent(elementsContent)
        assertTrue(elements is RpgElementArray<JsonObject>)
        assertEquals(2, elements.size)
        assertEquals("TestAbility", elements[0].data?.get("name")?.jsonPrimitive?.contentOrNull)
        assertEquals("TestRpgModel", elements[1].data?.get("name")?.jsonPrimitive?.contentOrNull)
        assertEquals(1, elements[1].data?.get("abilities")?.jsonArray?.get(0)?.jsonPrimitive?.intOrNull)

        val rpgElementCenter: RpgElementCenter<JsonObject> = CommonRpgElementCenter<JsonObject>().apply {
            elements.forEach {
                registerElement(it)
            }
        }

        val rpgObjectConstructorCenter: RpgObjConstructorCenter = CommonRpgObjConstructorCenter().apply {
            registerConstructor(TestAbilityConstructor())
            registerConstructor(TestRpgModelConstructor())
        }

        val rpgObjects = JsonRpgObjectProtocol.decodeFromContent(
            CommonRpgObjSerializeContext(
                rpgElementCenter,
                rpgObjectConstructorCenter
            ),
            dataContent
        )

        assertTrue(rpgObjects is RpgObjectArray)
        assertEquals(1, rpgObjects.size)
        assertTrue(rpgObjects[0] is RpgModel)
        val rpgModel = rpgObjects[0] as RpgModel
        assertEquals(2, rpgModel.abilities().size)
        assertEquals("TestRpgModel", rpgModel.meta().get<String>("name"))

        val testAbilities = rpgModel.abilities().filterIsInstance<TestAbility>()
        assertEquals(2, testAbilities.size)
        assertEquals(1, testAbilities[0].value)
        assertEquals(2, testAbilities[1].value)
        assertEquals("TestAbility", testAbilities[0].name)
        assertEquals("TestAbility", testAbilities[1].name)
    }

}