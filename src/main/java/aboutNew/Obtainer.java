package aboutNew;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

public class Obtainer {
    public static List<Map<String, String>> getTweets(String keyword, String username) throws Exception {

        Dotenv dotenv = Dotenv.load();
        String twitterToken = dotenv.get("BEARER_TOKEN");

        String nodePath = "c:\\Program Files\\nodejs\\node.exe";
        String scriptPath = "src\\main\\resources\\node\\simpleGetTweetScript.js";

        ProcessBuilder pb = new ProcessBuilder(
            nodePath,
            scriptPath, 
            keyword != null ? keyword : "",
            username != null ? username : ""
            //twitterToken != null ? twitterToken : ""
        );

        // pb.redirectErrorStream(true);
        Process process = pb.start();
    
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
    
        String outputStr = output.toString().trim();
        System.out.println("*******************Node OUTPUT:\n*************" + outputStr);
    
        if (!outputStr.startsWith("[") && !outputStr.startsWith("{")) {
            System.err.println("nodeJs script did not return valid JSON: " + outputStr);
            return Collections.singletonList(Map.of("error", "Invalid JSON from nodeJs script: " + outputStr));
        }
        
        try {
            JSONArray arr;
            if (outputStr.startsWith("{")) {
                // Wrap single object in array
                arr = new JSONArray();
                arr.put(new JSONObject(outputStr));
            } else {
                arr = new JSONArray(outputStr);
            }
        
            List<Map<String, String>> tweets = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Map<String, String> tweet = new HashMap<>();
                if (o.has("date")) tweet.put("date", o.getString("date"));
                if (o.has("content")) tweet.put("content", o.getString("content"));
                //if (o.has("error")) tweet.put("error", o.getString("error"));
                tweets.add(tweet);
            }
            return tweets;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonList(Map.of("error", e.getMessage()));
        }
    }
}
