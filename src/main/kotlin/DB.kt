import java.sql.DriverManager
import java.util.*

class DB {

    val url = "jdbc:postgresql://" + System.getenv("HOST") + "/" + System.getenv("POSTGRES_DB")
    private val props = Properties()

    init {
        props.setProperty("user", System.getenv("POSTGRES_USER"))
        props.setProperty("password", System.getenv("POSTGRES_PASSWORD"))
    }

    fun initTable() {
        DriverManager.getConnection(url, props).use { conn ->
            conn.createStatement().use { st ->
                st.execute(
                    "CREATE TABLE IF NOT EXISTS keys " +
                            "(id serial PRIMARY KEY, chatId varchar(50) NOT NULL UNIQUE, key varchar(200) NOT NULL);"
                )
            }
        }
    }

    fun addKey(chatId: String, vkKey: String) {
        DriverManager.getConnection(url, props).use { conn ->
            conn.prepareStatement(
                "INSERT INTO keys(chatId, key) VALUES (?, ?) " +
                        "ON CONFLICT (chatId) DO UPDATE SET key=EXCLUDED.key;"
            ).use { st ->
                st.setString(1, chatId)
                st.setString(2, vkKey)
                st.executeUpdate()
            }
        }
    }

    fun deleteKey(chatId: String): Int {
        var deleted = 0
        DriverManager.getConnection(url, props).use { conn ->
            conn.prepareStatement("DELETE FROM keys WHERE chatId = ?;").use { st ->
                st.setString(1, chatId)
                deleted = st.executeUpdate()
            }
        }
        return deleted
    }


    fun getVkKey(chatId: String): String {
        var res = ""
        DriverManager.getConnection(url, props).use { conn ->
            conn.prepareStatement("SELECT key FROM keys WHERE chatid = ?").use { st ->
                st.setString(1, chatId)
                st.executeQuery().use {
                    if (it.next()) res = it.getString(1)
                    else throw Exception("Please add a key for Vk api")
                }
            }
        }
        return res
    }

}