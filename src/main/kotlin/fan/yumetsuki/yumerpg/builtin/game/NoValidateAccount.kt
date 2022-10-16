package fan.yumetsuki.yumerpg.builtin.game

import fan.yumetsuki.yumerpg.game.RpgAccount
import fan.yumetsuki.yumerpg.game.RpgAccountCreator
import fan.yumetsuki.yumerpg.game.RpgAccountValidator
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.io.File

class NoValidateAccount(
    override val id: Long
) : RpgAccount

object NoValidateFileAccountManager {

    private val accountFile = File("account.json")
    private lateinit var accounts: MutableList<Long>

    suspend fun create(id: Long): RpgAccountCreator = object : RpgAccountCreator {

        override suspend fun create(): RpgAccount = coroutineScope {
            NoValidateAccount(id).apply {
                readAccounts().add(id)
                launch {
                    accountFile.writeText(
                        JsonArray(accounts.map { JsonPrimitive(it) }).toString()
                    )
                }
            }
        }

    }

    suspend fun validate(id: Long): RpgAccountValidator = object : RpgAccountValidator {

        override suspend fun validate(): RpgAccount {
            return readAccounts().find {
                it == id
            }?.let {
                NoValidateAccount(it)
            } ?: create(id).create()
        }

    }

    private fun readAccounts(): MutableList<Long> {
        if (!accountFile.exists()) {
            accountFile.createNewFile()
        }
        if (!this::accounts.isInitialized) {
            accounts = Json.parseToJsonElement(
                accountFile.readText()
            ).jsonArray.toList().map {
                it.jsonPrimitive.long
            }.toMutableList()
        }
        return accounts
    }

}