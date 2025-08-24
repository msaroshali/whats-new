package aboutNew.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import aboutNew.db.Database;
import aboutNew.model.Tweet;

public class TweetDAO {
    public static void saveTweet(Tweet tweet) {
        String sql = "INSERT OR IGNORE INTO tweets (username, content, date) VALUES (?, ?, ?)";

         try (Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) 
            {
            pstmt.setString(1, tweet.getUsername());
            pstmt.setString(2, tweet.getContent());
            pstmt.setString(3, tweet.getDate());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
