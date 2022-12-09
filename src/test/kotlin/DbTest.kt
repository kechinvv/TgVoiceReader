import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DbTest {
    val database = DB()

    @Test
    fun testFewKeyWithOneId() {
        val id = "111"
        val key1 = "aaaaaaaaaa"
        val key2 = "bbbbb_11"
        database.addKey(id, key1)
        database.addKey(id, key2)
        val deleted = database.deleteKey(id)
        assertEquals(1, deleted)
    }

    @Test
    fun testDeleteKeyIfExist() {
        val id = "111"
        val key = "aaaaaaaaaa"
        database.addKey(id, key)
        val deleted = database.deleteKey(id)
        assertEquals(1, deleted)
    }

    @Test
    fun testDeleteKeyNotExist() {
        val id = "111"
        val deleted = database.deleteKey(id)
        assertEquals(0, deleted)
    }

    @Test
    fun testAddKey() {
        val id = "111"
        val key = "aaaaaaaaaa"
        database.addKey(id, key)
        val actualKey = database.getVkKey(id)
        database.deleteKey(id)
        assertEquals(key, actualKey)
    }

    @Test
    fun testReplaceKey() {
        val id = "111"
        val key1 = "aaaaaaaaaa"
        val key2 = "bbbbb_11"
        database.addKey(id, key1)
        database.addKey(id, key2)
        val actualKey = database.getVkKey(id)
        database.deleteKey(id)
        assertEquals(key2, actualKey)
    }
}