package fan.yumetsuki.yumerpg.game

interface RpgAccount {
    val id: Long
}

interface RpgAccountCreator {

    suspend fun create(): RpgAccount

}

interface RpgAccountValidator {

    suspend fun validate(): RpgAccount

}
