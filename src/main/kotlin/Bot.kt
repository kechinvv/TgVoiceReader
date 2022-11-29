import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
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
        val url = "jdbc:postgresql://localhost:5432/KeysStorage"
        val props = Properties()
        props.setProperty("user", "postgres")
        props.setProperty("password", File("C:\\Users\\valer\\IdeaProjects\\TgVoiceReader\\db.txt").readText())
        conn = DriverManager.getConnection(url, props)
        val st = conn.createStatement()
        st.execute(
            "CREATE TABLE IF NOT EXISTS keys " +
                    "(id serial PRIMARY KEY, chatId varchar(50) NOT NULL UNIQUE, key varchar(200) NOT NULL);"
        )
        st.close()
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
                        addKey(getChatId(upd).toString(), upd.message.text)
                        silent.send("Key added successfully", getChatId(upd))
                    }, hasKey()
                )
            )
            .next(Reply.of({ _, upd -> silent.send("Key is invalid", getChatId(upd)) }, hasNotKey()))
            .build()
    }

    fun deleteFlow(): ReplyFlow {
        return ReplyFlow.builder(db)
            .action { _, upd ->
                deleteKey(getChatId(upd).toString())
                silent.send(
                    "Successfully deleted",
                    getChatId(upd)
                )
            }
            .onlyIf(hasMessageWith("/deletekey"))
            .build()
    }

    fun readVoiceFlow(): ReplyFlow {
        return ReplyFlow.builder(db)
            .action { _, upd ->
                readVoice(upd)
            }
            .onlyIf(hasVoice())
            .build()
    }

    private fun addKey(chatId: String, vkKey: String) {
        val st = conn.prepareStatement(
            "INSERT INTO keys(chatId, key) VALUES (?, ?) " +
                    "ON CONFLICT (chatId) DO UPDATE SET key=EXCLUDED.key;"
        )
        st.setString(1, chatId)
        st.setString(2, vkKey)
        st.executeUpdate()
        st.close()
    }

    private fun deleteKey(chatId: String) {
        val st = conn.prepareStatement("DELETE FROM keys WHERE chatId = ?;")
        st.setString(1, chatId)
        st.executeUpdate()
        st.close()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun readVoice(update: Update) {
        kotlinx.coroutines.GlobalScope.launch {
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
        }
    }

    private fun getVkKey(chatId: String): String {
        var res = ""
        val st = conn.prepareStatement("SELECT key FROM keys WHERE chatid = ?")
        st.setString(1, chatId)
        val rs = st.executeQuery()
        if (rs.next()) res = rs.getString(1)
        rs.close()
        st.close()
        return res
    }

    private fun downloadVoice(fileId: String): File {
        val uploadedFile = GetFile()
        uploadedFile.fileId = fileId
        val file = execute(uploadedFile)
        val localFile = File("localVoice/$fileId.ogg")
        val inpStr = URL(file.getFileUrl(token)).openStream()
        FileUtils.copyInputStreamToFile(inpStr, localFile)
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

