package aboutNew.controller;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import aboutNew.Obtainer;
import aboutNew.dao.TweetDAO;
import aboutNew.model.Tweet;
import io.javalin.http.Context;

public class TweetController {

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
                Tweet t = new Tweet(username, content, date);
                fetchedTweets.add(t);   
            }

            System.out.println("Saving tweets to database...");

            for (Tweet t : fetchedTweets) {
                TweetDAO.saveTweet(t);
            }
            System.out.println("Tweets saved to database successfully.");

            // Return JSON to frontend
            ctx.json(rawTweets);
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }
    public static void latest(Context ctx) {
        try {
            int limit = 10; // default
            String limitParam = ctx.queryParam("limit");
            if (limitParam != null) {
                limit = Integer.parseInt(limitParam);
            }
    
            List<Tweet> latestTweets = TweetDAO.getLatestTweets(limit);
    
            // Convert to JSON-friendly list of maps
            List<Map<String, String>> response = new ArrayList<>();
            for (Tweet t : latestTweets) {
                response.add(Map.of(
                    "username", t.getUsername(),
                    "content", t.getContent(),
                    "date", t.getDate()
                ));
            }
    
            ctx.json(response);
    
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }
}
