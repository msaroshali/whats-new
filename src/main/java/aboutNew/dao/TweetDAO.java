package aboutNew.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import aboutNew.db.Database;
import aboutNew.model.Tweet;

public class TweetDAO {
    public static int saveTweet(Tweet tweet) {
        String sql = "INSERT OR IGNORE INTO tweets (username, content, date) VALUES (?, ?, ?)";

         try (Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) 
            {
            pstmt.setString(1, tweet.getUsername());
            pstmt.setString(2, tweet.getContent());
            pstmt.setString(3, tweet.getDate());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

public static List<Tweet> getLatestTweets(int limit) {
        String sql = "SELECT username, content, date FROM tweets ORDER BY id DESC LIMIT ?";
        List<Tweet> tweets = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                String content = rs.getString("content");
                String date = rs.getString("date");
                tweets.add(new Tweet(username, content, date));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tweets;
    }

}
