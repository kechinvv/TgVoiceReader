import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.objects.ReplyFlow
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.File
import java.net.URL
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.function.Predicate

class Bot(val token: String, val username: String) : AbilityBot(token, username) {
    private lateinit var conn: Connection
    val vkApi = VKApi()

    override fun onRegister() {
        super.onRegister()
        val url = "jdbc:postgresql://"+System.getenv("HOST")+"/KeysStorage"
        val props = Properties()
        props.setProperty("user", System.getenv("POSTGRES_USER"))
        props.setProperty("password", System.getenv("POSTGRES_PASSWORD"))
        conn = DriverManager.getConnection(url, props)
        conn.createStatement().use { st ->
            st.execute(
                "CREATE TABLE IF NOT EXISTS keys " +
                        "(id serial PRIMARY KEY, chatId varchar(50) NOT NULL UNIQUE, key varchar(200) NOT NULL);"
            )
        }
    }

    override fun onClosing() {
        super.onClosing()
        conn.close()
    }


    fun registrationFlow(): ReplyFlow {
        return ReplyFlow.builder(db)
            .action { _, upd ->
                silent.send(
                    "Please, send VK Api token",
                    getChatId(upd)
                )
            }
            .onlyIf(hasMessageWith("/addkey"))
            .next(
                Reply.of(
                    { _, upd ->
                        try {
                            addKey(getChatId(upd).toString(), upd.message.text)
                            silent.send("Key added or replacement successfully", getChatId(upd))
                        } catch (e: Exception) {
                            silent.send(e.message, getChatId(upd))
                        }
                    }, hasKey()
                )
            )
            .next(Reply.of({ _, upd -> silent.send("Key is invalid", getChatId(upd)) }, hasNotKey()))
            .build()
    }

    fun deleteFlow(): ReplyFlow {
        return ReplyFlow.builder(db)
            .action { _, upd ->
                try {
                    val del = deleteKey(getChatId(upd).toString())
                    if (del == 1) silent.send("Successfully deleted", getChatId(upd))
                    else silent.send("There is no key to delete", getChatId(upd))
                } catch (e: Exception) {
                    silent.send(e.message, getChatId(upd))
                }
            }
            .onlyIf(hasMessageWith("/deletekey"))
            .build()
    }

    fun readVoiceFlow(): ReplyFlow {
        return ReplyFlow.builder(db)
            .action { _, upd ->
                try {
                    readVoice(upd)
                } catch (e: Exception) {
                    silent.send(e.message, getChatId(upd))
                }
            }
            .onlyIf(hasVoice())
            .build()
    }

    private fun addKey(chatId: String, vkKey: String) {
        conn.prepareStatement(
            "INSERT INTO keys(chatId, key) VALUES (?, ?) " +
                    "ON CONFLICT (chatId) DO UPDATE SET key=EXCLUDED.key;"
        ).use { st ->
            st.setString(1, chatId)
            st.setString(2, vkKey)
            st.executeUpdate()
        }
    }

    private fun deleteKey(chatId: String): Int {
        var deleted = 0
        conn.prepareStatement("DELETE FROM keys WHERE chatId = ?;").use { st ->
            st.setString(1, chatId)
            deleted = st.executeUpdate()
        }
        return deleted
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun readVoice(update: Update) {
        GlobalScope.launch {
            try {
                val file = downloadVoice(update.message.voice.fileId)
                val vkKey = getVkKey(update.message.chatId.toString())
                val uploadUrl = vkApi.getUploadUrl(vkKey)
                val urlResponse = vkApi.uploadVoice(uploadUrl, file, vkKey)
                val taskId = vkApi.getTaskId(urlResponse, vkKey)
                val result = vkApi.getTextFromVoice(vkKey, taskId)
                file.delete()
                silent.send(
                    result,
                    getChatId(update)
                )
            } catch (e: Exception) {
                silent.send(
                    e.message,
                    getChatId(update)
                )
            }
        }
    }

    private fun getVkKey(chatId: String): String {
        var res = ""
        conn.prepareStatement("SELECT key FROM keys WHERE chatid = ?").use { st ->
            st.setString(1, chatId)
            st.executeQuery().use {
                if (it.next()) res = it.getString(1)
                else throw Exception("Please add a key for Vk api")
            }
        }
        return res
    }

    private fun downloadVoice(fileId: String): File {
        val uploadedFile = GetFile()
        uploadedFile.fileId = fileId
        val file = execute(uploadedFile)
        val localFile = File("localVoice/$fileId.ogg")
        URL(file.getFileUrl(token)).openStream().use { inpStr ->
            FileUtils.copyInputStreamToFile(inpStr, localFile)
        }
        return localFile
    }

    override fun creatorId(): Long {
        return 248035752
    }

    private fun hasMessageWith(msg: String): Predicate<Update> {
        return Predicate { upd: Update ->
            upd.message.text.equals(msg, ignoreCase = true)
        }
    }

    private fun hasKey(): Predicate<Update> {
        return Predicate { upd: Update ->
            !upd.message.text.equals(
                "/addkey",
                ignoreCase = true
            ) && upd.message.text.length in 11..200 && !upd.message.text.contains(' ')
        }
    }

    private fun hasNotKey(): Predicate<Update> {
        return Predicate { upd: Update ->
            !upd.message.text.equals("/addkey", ignoreCase = true) && (upd.message.text.length <= 11
                    || upd.message.text.length >= 200 || upd.message.text.contains(' '))
        }
    }

    private fun hasVoice(): Predicate<Update> {
        return Predicate { upd: Update ->
            upd.message.hasVoice()
        }
    }
}

