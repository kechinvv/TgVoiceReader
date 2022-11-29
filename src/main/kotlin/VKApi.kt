import com.google.gson.JsonParser
import khttp.extensions.fileLike
import khttp.get
import khttp.post
import kotlinx.coroutines.delay
import java.io.File

class VKApi() {

    private val vkUrl = "https://api.vk.com/method/asr."
    fun getUploadUrl(token: String): String {
        val gsonText = get(url = vkUrl + "getUploadUrl", params = mapOf("access_token" to token, "v" to "5.131")).text
        return JsonParser.parseString(gsonText).asJsonObject.get("response").asJsonObject.get("upload_url").asString
    }

    fun uploadVoice(url: String, file: File, token: String): String {
        val p = post(url, headers = mapOf("access_token" to token), files = listOf(file.fileLike("file")))
        return p.text
    }

    fun getTaskId(upl_response: String, token: String): String {
        val gsonText = get(
            url = vkUrl + "process",
            params = mapOf("access_token" to token, "audio" to upl_response, "model" to "spontaneous", "v" to "5.131")
        ).text
        return JsonParser.parseString(gsonText).asJsonObject.get("response").asJsonObject.get("task_id").asString
    }

     suspend fun getTextFromVoice(token: String, taskId: String): String {
        var status = ""
        var gsonText = ""
        while (status == "" || status == "processing") {
            delay(1000)
            gsonText = get(
                url = vkUrl + "checkStatus",
                params = mapOf("access_token" to token, "task_id" to taskId, "v" to "5.131")
            ).text
            status = JsonParser.parseString(gsonText).asJsonObject.get("response").asJsonObject.get("status").asString
        }
        return JsonParser.parseString(gsonText).asJsonObject.get("response").asJsonObject.get("text").asString
    }
}