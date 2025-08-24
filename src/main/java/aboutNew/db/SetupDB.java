package aboutNew.db;

import java.sql.Connection;
import java.sql.Statement;
public class SetupDB {
    
    public static void init(){


       try (Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement()) 
            {

            System.out.println("Connection to SQLite has been established.");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tweets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT,
                    content TEXT,
                    date TEXT,
                    UNIQUE(content, date)
                )
            """);
            System.out.println("Table 'tweets' created or already exists.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




