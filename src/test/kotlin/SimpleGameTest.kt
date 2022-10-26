import fan.yumetsuki.yumerpg.SingleRpgGame
import fan.yumetsuki.yumerpg.builtin.RpgModel
import fan.yumetsuki.yumerpg.builtin.game.join
import fan.yumetsuki.yumerpg.builtin.game.start
import fan.yumetsuki.yumerpg.serialization.RpgObjectArray
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimpleGameTest {

    @Test
    fun testSimpleGame() {
        runBlocking {
            val saveFile = File("save/save-1.yumerpg")
            if (saveFile.exists()) {
                saveFile.delete()
            }
            val rpgElementFile = File("rpg_element.yumerpg").apply {
                if (!exists()) {
                    createNewFile()
                    writeText(elementsContent)
                }
            }
            val defaultDataFile = File("default_save.yumerpg").apply {
                if (!exists()) {
                    createNewFile()
                    writeText(dataContent)
                }
            }
            val game = SingleRpgGame.start {
                rpgElementFiles = listOf(rpgElementFile)
                this.defaultDataFile = defaultDataFile
                registerRpgObjectConstructor(
                    TestAbilityConstructor(),
                    TestRpgModelConstructor()
                )
                onInitRpgObject {
                    assertTrue(this is RpgObjectArray)
                    val rpgModel = this[0]
                    assertTrue(rpgModel is RpgModel)
                    rpgModel.abilities().filterIsInstance<TestAbility>().forEach {
                        it.value = 123456
                    }
                }
            }
            assertEquals(0, game.players().size)
            val player = game.join(1L)
            assertEquals(123456, Json.parseToJsonElement(
                saveFile.readBytes().decodeToString()
            ).jsonArray[0].jsonObject["data"]?.jsonObject?.get("abilities")?.jsonArray?.get(0)?.jsonObject?.get("data")?.jsonObject?.get("value")?.jsonPrimitive?.int!!)
            assertEquals(player, game.getPlayerOrNull(player.account))

            val model = (player.data() as RpgObjectArray)[0] as RpgModel
            assertEquals("TestRpgModel", model.meta().get<String>("name"))
            model.abilities().filterIsInstance<TestAbility>().forEach {
                it.value = 114514
            }

            player.save()
            player.exit()
            assertEquals(null, game.getPlayerOrNull(player.account))
            assertEquals(114514, Json.parseToJsonElement(
                saveFile.readBytes().decodeToString()
            ).jsonArray[0].jsonObject["data"]?.jsonObject?.get("abilities")?.jsonArray?.get(0)?.jsonObject?.get("data")?.jsonObject?.get("value")?.jsonPrimitive?.int!!)
            saveFile.parentFile.deleteRecursively()
            rpgElementFile.delete()
            defaultDataFile.delete()
        }
    }

}