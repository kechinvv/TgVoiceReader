import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.io.File

fun main() {
    try {
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        botsApi.registerBot(Bot(File("C:\\Users\\valer\\IdeaProjects\\TgVoiceReader\\data").readText(), "SecondEarsBot"))
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}

