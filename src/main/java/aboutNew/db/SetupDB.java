package aboutNew.db;

import java.sql.Connection;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetupDB {
    private static final Logger logger = LoggerFactory.getLogger(SetupDB.class);
    public static void init(){


       try (Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement()) 
            {

            logger.info("Connection to SQLite has been established.");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tweets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT,
                    content TEXT,
                    date TEXT,
                    source TEXT,
                    embedding BLOB,
                    UNIQUE(content, date)
                )
            """);
            logger.info("Table 'tweets' created or already exists.");

            try {
                stmt.execute("ALTER TABLE tweets ADD COLUMN embedding BLOB");
                logger.info("Added 'embedding' column to table 'tweets'.");
            } catch (Exception e) {
                // Column might already exist, ignore error
            }

        } catch (Exception e) {
            logger.error("Database initialization failed", e);
        }
    }
}




