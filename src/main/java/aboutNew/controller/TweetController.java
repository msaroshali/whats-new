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

    public static void timeframeSummary(Context ctx) {
        String timeframe = ctx.queryParam("timeframe");
        if (timeframe == null || timeframe.trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", "Query parameter 'timeframe' is required."));
            return;
        }

        try {
            // Retrieve latest 3000 tweets
            List<Tweet> allTweets = TweetDAO.getLatestTweets(3000, null);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
            
            Instant now = Instant.now();
            Instant cutoff;
            if ("hour".equalsIgnoreCase(timeframe)) {
                cutoff = now.minus(1, ChronoUnit.HOURS);
            } else if ("day".equalsIgnoreCase(timeframe)) {
                cutoff = now.minus(24, ChronoUnit.HOURS);
            } else if ("week".equalsIgnoreCase(timeframe)) {
                cutoff = now.minus(7, ChronoUnit.DAYS);
            } else {
                ctx.status(400).json(Map.of("error", "Invalid timeframe parameter. Use 'hour', 'day', or 'week'."));
                return;
            }

            // Filter tweets within timeframe
            List<Tweet> filteredTweets = new ArrayList<>();
            for (Tweet t : allTweets) {
                if (t.getDate() == null || t.getDate().trim().isEmpty()) continue;
                try {
                    ZonedDateTime tweetTime = ZonedDateTime.parse(t.getDate(), formatter);
                    if (tweetTime.toInstant().isAfter(cutoff)) {
                        filteredTweets.add(t);
                    }
                } catch (Exception parseEx) {
                    // Log parsing error but don't break execution
                    logger.debug("Failed to parse tweet date: {}", t.getDate(), parseEx);
                }
            }

            if (filteredTweets.isEmpty()) {
                ctx.contentType("application/json; charset=utf-8");
                ctx.json(Map.of("answer", "No news was recorded in the database for the past " + timeframe + "."));
                return;
            }

            // Construct context from filtered tweets
            StringBuilder contextBuilder = new StringBuilder();
            for (Tweet t : filteredTweets) {
                contextBuilder.append("- [@").append(t.getUsername())
                              .append("] (").append(t.getDate()).append("): ")
                              .append(t.getContent());
                if (t.getSource() != null && !t.getSource().trim().isEmpty()) {
                    contextBuilder.append(" [Source Link](").append(t.getSource()).append(")");
                }
                contextBuilder.append("\n\n");
            }

            String context = contextBuilder.toString();
            String prompt = "## SYSTEM INSTRUCTIONS\n"
                    + "You are an AI news summarizer informing a user about current affairs based on tweets from the [TWEET CONTEXT] below.\n"
                    + "The user requested a summary for the timeframe: past " + timeframe + ".\n"
                    + "Generate a beautifully structured summary of the news within this timeframe. Follow these rules:\n"
                    + "1. Summarize the major events clearly, grouping them under relevant topic categories (e.g. **Geopolitics**, **Tech**, **Science**, etc.) as markdown headers.\n"
                    + "2. Use Markdown lists and bold text for key points to ensure maximum scannability.\n"
                    + "3. Chronologically sort events where appropriate, starting with the most recent.\n"
                    + "4. Cite sources using clickable Markdown hyperlinks format: `[@username](sourceUrl)` where applicable.\n"
                    + "5. If there are no relevant events or details, output exactly: \"No news was recorded in the database for the past " + timeframe + ".\"\n\n"
                    + "## DATA\n"
                    + "[TWEET CONTEXT]\n"
                    + (context.isEmpty() ? "(No context tweets available.)" : context) + "\n\n"
                    + "Please synthesize this data into a coherent and premium summary.";
            
            
            //sent tweets
            // Log top 10 matches to the console for monitoring
            logger.info("News from past '{}':", timeframe);
            logger.info("Total news sent: '{}'", filteredTweets.size());
            logger.info("Top 10:");
            for (int i = 0; i < Math.min(10, filteredTweets.size()); i++) {
                Tweet t = filteredTweets.get(i);
                logger.info(" - @{}: {}", t.getUsername(), t.getContent());
            }

            String summary = GeminiService.askGemini(prompt);
            
            if ("RETRY_503".equals(summary)) {
                ctx.status(503).json(Map.of("error", "503 Service Unavailable: Model overloaded. Switching model and retrying...", "retry", true));
                return;
            }


            String formattedSummary = formatMarkdownToServerSideHTML(summary);
            ctx.contentType("application/json; charset=utf-8");
            ctx.json(Map.of("answer", formattedSummary));

        } catch (Exception e) {
            logger.error("Error in timeframeSummary endpoint", e);
            ctx.status(500).json(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

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

    public static void aiSearch(Context ctx) {
        String question = ctx.queryParam("q");
        if (question == null || question.trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", "Query parameter 'q' is required."));
            return;
        }

        try {
            float[] queryEmbedding = EmbeddingService.getEmbedding(question);
            if (queryEmbedding == null) {
                ctx.status(503).json(Map.of("error", "Failed to generate query embedding. The embedding API quota might be exhausted."));
                return;
            }

            //  Fetch all tweets with embeddings (limit to 1000 for search relevance)
            List<Tweet> allTweets = TweetDAO.getTweetsForSearch(1000);

            //Compute cosine similarity for each tweet
            List<ScoredTweet> scoredTweets = new ArrayList<>();
            for (Tweet t : allTweets) {
                double score = computeCosineSimilarity(queryEmbedding, t.getEmbedding());
                scoredTweets.add(new ScoredTweet(t, score));
            }

            // Sort in descending order
            Collections.sort(scoredTweets);

            // log top 15 matches to the console for monitoring
            logger.info("Top matches for AI query: '{}'", question);
            for (int i = 0; i < Math.min(15, scoredTweets.size()); i++) {
                ScoredTweet st = scoredTweets.get(i);
                logger.info(" - [{}] @{}: {}", String.format("%.4f", st.score), st.tweet.getUsername(), st.tweet.getContent());
            }

            // Build prompt using top 60 matches (or fewer if DB doesn't have 60)
            int numMatches = Math.min(60, scoredTweets.size());
            StringBuilder contextBuilder = new StringBuilder();
            for (int i = 0; i < numMatches; i++) {
                ScoredTweet st = scoredTweets.get(i);
                contextBuilder.append("- [@").append(st.tweet.getUsername())
                              .append("] (").append(st.tweet.getDate()).append("): ")
                              .append(st.tweet.getContent()).append("\n\n");
            }

            String context = contextBuilder.toString();
            // String prompt = "You are an AI assistant helping a user analyze current affairs based on tweets gathered from X/Twitter.\n"
            //         + "Answer the user's question using only the context of the tweets provided below.\n"
            //         + "Provide a comprehensive, accurate answer structured in clear Markdown (use lists, bold text, etc., where appropriate).\n"
            //         + "Reference the usernames or tweet dates inside your answer where helpful.\n"
            //         + "If the tweets do not contain relevant information to answer the question, output exactly: \"I couldn't find relevant information in the database to answer that question.\"\n\n"
            //         + "[TWEET CONTEXT]\n"
            //         + (context.isEmpty() ? "(No context tweets available.)" : context) + "\n\n"
            //         + "[USER QUESTION]\n"
            //         + question;
            String prompt = "## SYSTEM INSTRUCTIONS\n"
                    + "You are an AI assistant informing a user about current affairs and news based only on tweets from the [TWEET CONTEXT] below.\n"
                    + "Answer the user's question using only the context of the tweets provided. Follow these formatting rules strictly:\n"
                    + "1. Start with a brief summary that is easy to read with highlighted text.\n"
                    + "2. Provide a comprehensive, accurate answer structured in clear Markdown (use lists, bold text, etc., where appropriate) in descending order of date/time.\n"
                    + "3. Reference the usernames inside your answer where helpful.\n"
                    + "4. Use Markdown lists and bold text for scannability.\n"
                    + "5. CRITICAL: Every tweet citation must be a clickable hyperlink using standard Markdown syntax `[Anchor Text](URL)` followed by the date.\n\n"

                    + "## CITATION EXAMPLE\n"
                    + "When referencing a tweet, format it exactly like this:\n"
                    + "News about lorem ipsum **@username** [Source Link] [Date: 2021-01-01]\n\n"
                    
                    + "## FALLBACK RULES\n"
                    + "Output exactly \"I couldn't find relevant information in the database to answer that question.\" if:\n"
                    + "- The tweets do not contain relevant information to answer the question.\n"
                    + "- The [USER QUESTION] is requesting something outside the context of the provided news.\n\n"
                    
                    + "## DATA\n"
                    + "[TWEET CONTEXT]\n"
                    + (context.isEmpty() ? "(No context tweets available.)" : context) + "\n\n"
                    + "[USER QUESTION]\n"
                    + question;



            //  Query Gemini
            String answer = GeminiService.askGemini(prompt);

            if ("RETRY_503".equals(answer)) {
                ctx.status(503).json(Map.of("error", "503 Service Unavailable: Model overloaded. Switching model and retrying...", "retry", true));
                return;
            }

            String formattedAnswer = formatMarkdownToServerSideHTML(answer);
            // 7. Return answer
            ctx.contentType("application/json; charset=utf-8");
            ctx.json(Map.of("answer", formattedAnswer));

        } catch (Exception e) {
            logger.error("Error in aiSearch endpoint", e);
            ctx.status(500).json(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    private static double computeCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            return 0.0;
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static class ScoredTweet implements Comparable<ScoredTweet> {
        Tweet tweet;
        double score;

        ScoredTweet(Tweet tweet, double score) {
            this.tweet = tweet;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredTweet o) {
            return Double.compare(o.score, this.score); // Descending order
        }
    }
}
