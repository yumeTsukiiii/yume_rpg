import fan.yumetsuki.yumerpg.core.serialization.*
import fan.yumetsuki.yumerpg.core.serialization.protocol.JsonRpgElementProtocol
import fan.yumetsuki.yumerpg.core.serialization.protocol.JsonRpgObjectData
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.expect

class TestAbility(
    override val elementId: Long,
    override val name: String,
    override var value: Int
) : PropertyAbility<Int, RpgModel>

class TestAbilityConstructor : RpgObjectConstructor<JsonObject> {

    override val id: Long
        get() = 1

    override fun construct(context: RpgObjectContextWithData<JsonObject>): RpgObject {
        return TestAbility(
            elementId = context.elementId,
            name = context.getStringOrNull("name")!!,
            value = context.getIntOrNull("value")!!
        )
    }

    override fun deconstruct(context: RpgObjectContext<JsonObject>, rpgObject: RpgObject): RpgObjectData<JsonObject> {
        if (rpgObject !is TestAbility) error("不是 TestAbility")

        return JsonRpgObjectData(id).apply {
            put("name", JsonPrimitive(rpgObject.name))
            put("value", JsonPrimitive(rpgObject.value))
        }
    }

}

class TestRpgModelConstructor : RpgObjectConstructor<JsonObject> {

    override val id: Long
        get() = 2

    override fun construct(context: RpgObjectContextWithData<JsonObject>): RpgObject {
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

    override fun deconstruct(context: RpgObjectContext<JsonObject>, rpgObject: RpgObject): RpgObjectData<JsonObject> {
        if (rpgObject !is CommonRpgModel) error("不是 CommonRpgModel")

        return JsonRpgObjectData(rpgObject.elementId).apply {
            put("name", JsonPrimitive(rpgObject.meta().get<String>("name")))
            put("abilities", RpgObjectDataArray(
                rpgObject.abilities().map {
                    context.deconstructRpgObject(it)!!
                }
            ))
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
    }

}