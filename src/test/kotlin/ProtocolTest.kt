import fan.yumetsuki.yumerpg.builtin.*
import fan.yumetsuki.yumerpg.ecs.ECSComponent
import fan.yumetsuki.yumerpg.ecs.ECSEntity
import fan.yumetsuki.yumerpg.serialization.protocol.JsonRpgElementProtocol
import fan.yumetsuki.yumerpg.serialization.protocol.JsonRpgObjectProtocol
import fan.yumetsuki.yumerpg.serialization.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestComponent(
    override val elementId: Long,
    val name: String,
    var value: Int
) : RpgComponent

class TestComponentConstructor : RpgObjectConstructor {

    override val id: Long
        get() = ID

    override suspend fun construct(context: RpgObjectConstructContext): RpgObject {
        return TestComponent(
            elementId = context.elementId,
            name = context.getStringOrNull("name")!!,
            value = context.getLongOrNull("value")?.toInt() ?: 10
        )
    }

    override suspend fun deconstruct(context: RpgObjectDeconstructContext) {
        val rpgObject = context.rpgObject<TestComponent>()
        context.deconstruct {
            put("name", rpgObject.name)
            put("value", rpgObject.value)
        }
    }

    companion object {
        const val ID = 1L
    }

}

val elementsContent = """
                [
                    {
                        "id": 1,
                        "constructor": 1,
                        "data": {
                            "name": "TestComponent"
                        }
                    },
                    {
                        "id": 2,
                        "constructor": 2,
                        "data": {
                            "name": "TestRpgEntity",
                            "components": [
                                1
                            ]
                        }
                    }
                ]
            """.trimIndent()

val dataContent = """
                [
                    {
                        "element": 2,
                        "data": {
                            
                        }
                    }
                ]
            """.trimIndent()

class TestRpgEntityConstructor : RpgObjectConstructor {

    override val id: Long
        get() = ID

    override suspend fun construct(context: RpgObjectConstructContext): RpgObject {
        return ListRpgEntity(
            elementId = context.elementId,
            name = context.getString("name"),
            components = (
                context.getRpgObjectOrNull("components")!! as RpgObjectArray
            ).filterIsInstance<RpgComponent>()
        )
    }

    override suspend fun deconstruct(context: RpgObjectDeconstructContext) {
        val rpgObject = context.rpgObject<ECSEntity>()

        context.deconstruct {
            put("name", rpgObject.name)
            put("components", RpgObjectArray(rpgObject.components().filterIsInstance<RpgComponent>()) as RpgObject)
        }
    }

    companion object {
        const val ID = 2L
    }

}

class ProtocolTest {

    @Test
    fun testJsonProtocol() = runBlocking {
        val elements = JsonRpgElementProtocol.decodeFromContent(elementsContent)
        assertTrue(elements is RpgElementArray)
        assertEquals(2, elements.size)
        assertEquals("TestComponent", elements[0].getStringOrNull("name"))
        assertEquals("TestRpgEntity", elements[1].getStringOrNull("name"))
        assertEquals(1, elements[1].getStringOrNull("components")?.let {
            Json.parseToJsonElement(it).jsonArray.size
        })

        val rpgElementCenter: RpgElementCenter = CommonRpgElementCenter().apply {
            elements.forEach {
                registerElement(it)
            }
        }

        val rpgObjectConstructorCenter: RpgObjConstructorCenter = CommonRpgObjConstructorCenter().apply {
            registerConstructor(TestComponentConstructor())
            registerConstructor(TestRpgEntityConstructor())
        }

        val serializeContext = CommonRpgObjSerializeContext(
            rpgElementCenter,
            rpgObjectConstructorCenter
        )

        val rpgObjects = JsonRpgObjectProtocol.decodeFromContent(
            serializeContext,
            dataContent
        )

        assertTrue(rpgObjects is RpgObjectArray)
        assertEquals(1, rpgObjects.size)
        assertTrue(rpgObjects[0] is RpgEntity)
        val rpgModel = rpgObjects[0] as RpgEntity
        assertEquals(1, rpgModel.components().size)
        assertEquals("TestRpgEntity", rpgModel.name)

        val testAbilities = rpgModel.components().filterIsInstance<TestComponent>()
        assertEquals(1, testAbilities.size)
        assertEquals(10, testAbilities[0].value)
        assertEquals("TestComponent", testAbilities[0].name)


        testAbilities[0].value = 3
        val rpgObjectsJsonArray = Json.parseToJsonElement(
            JsonRpgObjectProtocol.encodeToContent(
                serializeContext,
                rpgObjects
            )
        )
        assertTrue(rpgObjectsJsonArray is JsonArray)
        assertEquals(1, rpgObjectsJsonArray.size)
        val rpgModelJson = rpgObjectsJsonArray[0].jsonObject
        assertEquals(2, rpgModelJson["element"]?.jsonPrimitive?.intOrNull)
        val rpgModelData = rpgModelJson["data"]?.jsonObject
        assertTrue(rpgModelData != null)
        val abilitiesJson = rpgModelData["components"] as? JsonArray
        assertTrue(abilitiesJson != null)
        val abilitiesJsonList = abilitiesJson.filterIsInstance<JsonObject>()
        assertEquals(1, abilitiesJsonList.size)

        val testAbilityFirst = abilitiesJsonList[0]

        assertEquals(1, testAbilityFirst["element"]?.jsonPrimitive?.intOrNull)
        assertEquals("TestComponent", testAbilityFirst["data"]?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull)
        assertEquals(3, testAbilityFirst["data"]?.jsonObject?.get("value")?.jsonPrimitive?.intOrNull)
    }

}