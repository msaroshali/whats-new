package aboutNew.controller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import aboutNew.Obtainer;
import aboutNew.EmbeddingService;
import aboutNew.GeminiService;
import aboutNew.dao.TweetDAO;
import aboutNew.model.Tweet;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class TweetController {
    private static final Logger logger = LoggerFactory.getLogger(TweetController.class);


    //Breaking
    public static void search(Context ctx)
    {
        String keyword = ctx.queryParam("keyword");
        String username = ctx.queryParam("username");

        try {

            List<Map<String, String>> rawTweets = Obtainer.getTweets(keyword, username);
            // convert to model
            List<Tweet> fetchedTweets = new ArrayList<>();
            for(Map<String, String> map : rawTweets)
            {
                String date = map.getOrDefault("date","");
                String content = map.getOrDefault("content", "");
                String source = map.getOrDefault("sourceUrl", "");
                Tweet t = new Tweet(username, content, date, source);
                fetchedTweets.add(t);   
            }

            logger.info("Breaking Tweets fetched successfully. Total: {}", fetchedTweets.size());
            logger.info("Saving only newer tweets to database...");
            
            int insertedCount = 0;
            for (Tweet t : fetchedTweets) {
                insertedCount += TweetDAO.saveTweet(t);
            }

            logger.info("Inserted: {}, Ignored: {}", insertedCount, (rawTweets.size() - insertedCount));
            logger.info("Process execution successful for {}", username.toUpperCase());
            
            // Return JSON to frontend
            ctx.contentType("application/json; charset=utf-8");
            ctx.json(rawTweets);
            
        } catch (Exception e) {
            logger.error("Error in search endpoint", e);
            ctx.status(500).json(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }
    
    public static void latest(Context ctx) {
                String username = ctx.queryParam("username");

        try {
            int limit = 200; // default
            String limitParam = ctx.queryParam("limit");
            if (limitParam != null) {
                limit = Integer.parseInt(limitParam);
            }
    
            List<Tweet> latestTweets = TweetDAO.getLatestTweets(limit, username);
    
            // Convert to JSON-friendly list of maps for displaying on the frontend
            List<Map<String, String>> response = new ArrayList<>();
            for (Tweet t : latestTweets) {
                response.add(Map.of(
                    "username", Objects.toString(t.getUsername(), ""),
                    "content", Objects.toString(t.getContent(), ""),
                    "date", Objects.toString(t.getDate(), ""),
                    "sourceUrl", Objects.toString(t.getSource(), "") // saame as "sourceUrl", t.getSource() == null ? "" : t.getSource()
                ));
            }
            ctx.contentType("application/json; charset=utf-8");
            ctx.json(response);
    
        } catch (Exception e) {
            logger.error("Error in latest endpoint", e);
            ctx.status(500).json(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    public static void ticker(Context ctx) {
        try {
            List<Tweet> tickerTweets = TweetDAO.getTickerTweets();
            Collections.shuffle(tickerTweets); // Randomize the tweets

            List<Map<String, String>> response = new ArrayList<>();
            for (Tweet t : tickerTweets) {
                response.add(Map.of(
                    "username", Objects.toString(t.getUsername(), ""),
                    "content", Objects.toString(t.getContent(), ""),
                    "date", Objects.toString(t.getDate(), ""),
                    "sourceUrl", Objects.toString(t.getSource(), "")
                ));
            }
            ctx.contentType("application/json; charset=utf-8");
            ctx.json(response);
        } catch (Exception e) {
            logger.error("Error in ticker endpoint", e);
            ctx.status(500).json(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    public static String formatMarkdownToServerSideHTML(String text) {
        if (text == null) return "";
        
        // Escape HTML to prevent XSS
        String html = text.replace("&", "&amp;")
                          .replace("<", "&lt;")
                          .replace(">", "&gt;");
                          
        // Convert links [text](url) - tolerate spaces between ] and (
        html = html.replaceAll("\\[([^\\]]+)\\]\\s*\\(([^)]+)\\)", "<a href=\"$2\" target=\"_blank\" class=\"text-blue-500 underline hover:text-blue-700\">$1</a>");
        
        // Convert bold **text**
        html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");
        
        // Convert inline code `code`
        html = html.replaceAll("`(.*?)`", "<code>$1</code>");
        
        // Process line by line for blocks (headings, lists, paragraphs)
        StringBuilder sb = new StringBuilder();
        String[] lines = html.split("\n");
        boolean inList = false;
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            if (trimmed.isEmpty()) {
                if (inList) {
                    sb.append("</ul>");
                    inList = false;
                }
                continue;
            }
            
            if (trimmed.startsWith("### ")) {
                if (inList) {
                    sb.append("</ul>");
                    inList = false;
                }
                sb.append("<h3 class=\"text-lg font-bold mt-4 mb-2 text-blue-600 dark:text-blue-400\">")
                  .append(trimmed.substring(4)).append("</h3>");
            } else if (trimmed.startsWith("## ")) {
                if (inList) {
                    sb.append("</ul>");
                    inList = false;
                }
                sb.append("<h2 class=\"text-xl font-bold mt-5 mb-3 text-blue-700 dark:text-blue-300\">")
                  .append(trimmed.substring(3)).append("</h2>");
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                if (!inList) {
                    sb.append("<ul class=\"list-disc pl-5 my-2 space-y-1\">");
                    inList = true;
                }
                sb.append("<li>").append(trimmed.substring(2)).append("</li>");
            } else {
                if (inList) {
                    sb.append("</ul>");
                    inList = false;
                }
                sb.append("<p class=\"mb-3 leading-relaxed\">").append(trimmed).append("</p>");
            }
        }
        
        if (inList) {
            sb.append("</ul>");
        }
        
        return sb.toString();
    }

    public static void fetchAndSaveDefaultSources() {
        try {
            String[] usernames = {"faytuksnetwork", "clashreport",  "Wccftech", "TechCrunch", "verge", "engadget", "TechRadar", "Gizmodo", "TheNextWeb", "DigitalTrends"};

            for (String username : usernames) {
                List<Map<String, String>> rawTweets = Obtainer.getTweets("", username);

                if (!rawTweets.isEmpty()) {
                    logger.info("Auto fetch of Tweets successfully for {}. Total: {}", username.toUpperCase(), rawTweets.size());
                    logger.info("Saving only newer tweets to database...");
                    int insertedCount = 0;
                    for (Map<String, String> map : rawTweets) {
                        String date = map.getOrDefault("date", "");
                        String content = map.getOrDefault("content", "");
                        String source = map.getOrDefault("sourceUrl", "");
                        Tweet t = new Tweet(username, content, date, source );

                        insertedCount += TweetDAO.saveTweet(t);


                    }

                    logger.info("Inserted: {}, Ignored: {}", insertedCount, (rawTweets.size() - insertedCount));
                    logger.info("Process execution successful for {}", username.toUpperCase());

                } 
                else {
                    logger.info("No tweets found for {}. Skipping...", username.toUpperCase());
                }

                Thread.sleep(25000);


            }
        } catch (Exception e) {
            logger.error("Error in fetchAndSaveDefaultSources", e);
        }
    }

}
