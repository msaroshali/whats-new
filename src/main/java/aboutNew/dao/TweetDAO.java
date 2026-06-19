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
        String sql = "INSERT OR IGNORE INTO tweets (username, content, date, source) VALUES (?, ?, ?, ?)";

         try (Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) 
            {
            pstmt.setString(1, tweet.getUsername());
            pstmt.setString(2, tweet.getContent());
            pstmt.setString(3, tweet.getDate());
            pstmt.setString(4, tweet.getSource());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

public static List<Tweet> getLatestTweets(int limit, String usernameQ) {
        
 String sql;
    List<Tweet> tweets = new ArrayList<>();

    try (Connection conn = Database.getConnection()) {
        PreparedStatement pstmt;
        if (usernameQ == null || usernameQ.isEmpty()) {
            sql = "SELECT username, content, date, source FROM tweets ORDER BY id DESC LIMIT ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
        } else {
            sql = "SELECT username, content, date, source FROM tweets WHERE username = ? ORDER BY id DESC LIMIT ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, usernameQ);
            pstmt.setInt(2, limit);
        }

        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            String username = rs.getString("username");
            String content = rs.getString("content");
            String date = rs.getString("date");
            String source = rs.getString("source");
            tweets.add(new Tweet(username, content, date, source));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return tweets;
    }

}
