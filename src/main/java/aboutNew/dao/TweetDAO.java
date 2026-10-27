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

    public static List<Tweet> getTweetsForSearch(int limit) {
        String sql = "SELECT id, username, content, date, source, embedding FROM tweets WHERE embedding IS NOT NULL ORDER BY id DESC LIMIT ?";
        List<Tweet> tweets = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
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
            logger.error("Error fetching tweets for search", e);
        }

        return tweets;
    }

    public static List<Tweet> getTickerTweets() {
        List<Tweet> tweets = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            // Get all distinct usernames
            String distinctUsersSql = "SELECT DISTINCT username FROM tweets";
            List<String> usernames = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(distinctUsersSql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    usernames.add(rs.getString(1));
                }
            }
            
            // For each username, get latest 3 tweets
            String sql = "SELECT id, username, content, date, source FROM tweets WHERE username = ? ORDER BY id DESC LIMIT 3";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String username : usernames) {
                    pstmt.setString(1, username);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            String user = rs.getString("username");
                            String content = rs.getString("content");
                            String date = rs.getString("date");
                            String source = rs.getString("source");
                            
                            Tweet t = new Tweet(user, content, date, source);
                            t.setId(id);
                            tweets.add(t);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching ticker tweets", e);
        }

        return tweets;
    }

    public static List<Tweet> getTweetsMissingEmbeddings(int limit) {
        String sql = "SELECT id, username, content, date, source FROM tweets WHERE (embedding IS NULL OR length(embedding) != 12288) AND content !='' ORDER BY id DESC LIMIT ?";
        List<Tweet> tweets = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String content = rs.getString("content");
                    String date = rs.getString("date");
                    String source = rs.getString("source");
                    
                    Tweet t = new Tweet(username, content, date, source);
                    t.setId(id);
                    tweets.add(t);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching tweets missing embeddings", e);
        }

        return tweets;
    }

    public static void updateTweetEmbedding(int id, float[] embedding) {
        String sql = "UPDATE tweets SET embedding = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBytes(1, floatArrayToByteArray(embedding));
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            logger.error("Error updating tweet embedding for id {}", id, e);
        }
    }

    // Helper methods for converting float[] to byte[] and back
    private static byte[] floatArrayToByteArray(float[] floats) {
        if (floats == null) return null;
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(floats.length * 4);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    private static float[] byteArrayToFloatArray(byte[] bytes) {
        if (bytes == null) return null;
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(bytes);
        float[] floats = new float[bytes.length / 4];
        for (int i = 0; i < floats.length; i++) {
            floats[i] = buffer.getFloat();
        }
        return floats;
    }

    private static boolean isZeroVector(float[] arr) {
        if (arr == null) return true;
        for (float f : arr) {
            if (f != 0.0f) return false;
        }
        return true;
    }

    public static void startBackgroundEmbeddingMigration() {
        new Thread(() -> {
            try {
                logger.info("[Migration] Checking for historical tweets missing embeddings...");
                // Wait 10 seconds for the application startup to settle
                Thread.sleep(3000);
                
                List<Tweet> missing = getTweetsMissingEmbeddings(50);
                AtomicInteger embedCount = new AtomicInteger(0);

                while (!missing.isEmpty()) {
                    logger.info("[Migration] Found {} historical tweets missing/incorrect embeddings. Processing them slowly...", missing.size());
                    for (Tweet t : missing) {
                        logger.info("[Migration] Generating embedding for tweet ID {} - @{} (Length Check/Null Failure)", t.getId(), t.getUsername());
                        float[] emb = EmbeddingService.getEmbedding(t.getContent());
                        if (emb != null && emb.length > 0 && !isZeroVector(emb)) {
                            updateTweetEmbedding(t.getId(), emb);
                            embedCount.incrementAndGet();
                            logger.info("[Migration] Successfully updated embedding for tweet ID {}. (Total: {} embedded)", t.getId(), embedCount.get());
                        } else {
                            logger.warn("[Migration] Failed to generate valid embedding for tweet ID {}", t.getId());
                        }
                        // Sleep to respect standard API rate limits 
                        Thread.sleep(4500);
                    }
                    missing = getTweetsMissingEmbeddings(50);
                }
                logger.info("[Migration] All historical tweets successfully embedded.");
            } catch (InterruptedException e) {
                logger.info("[Migration] Background embedding thread interrupted.");
            } catch (Exception e) {
                logger.error("[Migration] Error in background embedding: {}", e.getMessage(), e);
            }
        }).start();
    }
}
