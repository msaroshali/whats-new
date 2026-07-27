package aboutNew.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import aboutNew.db.Database;
import aboutNew.model.Tweet;
import aboutNew.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TweetDAO {
    private static final Logger logger = LoggerFactory.getLogger(TweetDAO.class);

    public static int saveTweet(Tweet tweet) {
        // Automatically fetch embedding if missing
        if (tweet.getEmbedding() == null) {
            float[] emb = EmbeddingService.getEmbedding(tweet.getContent());
            tweet.setEmbedding(emb);
        }

        String sql = "INSERT OR IGNORE INTO tweets (username, content, date, source, embedding) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tweet.getUsername());
            pstmt.setString(2, tweet.getContent());
            pstmt.setString(3, tweet.getDate());
            pstmt.setString(4, tweet.getSource());
            pstmt.setBytes(5, floatArrayToByteArray(tweet.getEmbedding()));
            return pstmt.executeUpdate();
        } catch (Exception e) {
            logger.error("Error saving tweet", e);
            return 0;
        }
    }

    public static List<Tweet> getLatestTweets(int limit, String usernameQ) {
        String sql;
        List<Tweet> tweets = new ArrayList<>();

        try (Connection conn = Database.getConnection()) {
            PreparedStatement pstmt;
            if (usernameQ == null || usernameQ.isEmpty()) {
                sql = "SELECT id, username, content, date, source, embedding FROM tweets ORDER BY id DESC LIMIT ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, limit);
            } else {
                sql = "SELECT id, username, content, date, source, embedding FROM tweets WHERE username = ? ORDER BY id DESC LIMIT ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, usernameQ);
                pstmt.setInt(2, limit);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String content = rs.getString("content");
                    String date = rs.getString("date");
                    String source = rs.getString("source");
                    byte[] embBytes = rs.getBytes("embedding");
                    float[] emb = byteArrayToFloatArray(embBytes);
                    
                    Tweet t = new Tweet(username, content, date, source, emb);
                    t.setId(id);
                    tweets.add(t);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching latest tweets", e);
        }

        return tweets;
    }

}
