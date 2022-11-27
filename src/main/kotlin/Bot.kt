import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File


class Bot : TelegramLongPollingBot() {

    override fun onUpdateReceived(update: Update?) {
        if (update != null) {
            if (update.hasMessage())
                if (update.message.hasText()) {
                    val chatId = update.message.chatId.toString()
                    val args = update.message.text.replace(" +", " ").split(' ')
                    when (args[0]) {
                        "/addtoken" -> addToken(chatId, args)
                    }

                }

        }
    }

    fun addToken(chatId: String, args: List<String>) {
        if (args.size != 2) sendMessage(chatId, "Please, add key in command")
        else {
            TODO()
        }
    }

    fun sendMessage(chatId: String, msg: String) {
        val message = SendMessage()
        message.chatId = chatId
        message.text = msg
        try {
            execute(message) // Call method to send the message
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun getBotUsername(): String {
        return "SecondEarsBot"
    }

    override fun getBotToken(): String {
        return File("C:\\Users\\valer\\IdeaProjects\\TgVoiceReader\\data").readText()
    }

}

