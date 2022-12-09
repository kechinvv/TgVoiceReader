import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class VkApiErrorsTest {
    val vkApi = VKApi()

    @Test
    fun wrongKey() {
        assertThrows<Exception> {
            vkApi.getUploadUrl("ssss")
        }
    }

    @Test
    fun testGetUploadUrl() {
        assertDoesNotThrow {
            vkApi.getUploadUrl(System.getenv("VK_API"))
        }
    }
}