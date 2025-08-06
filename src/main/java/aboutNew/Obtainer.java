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

public class Obtainer {
    public static List<Map<String, String>> getTweets(String keyword, String username) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Users\\PC 3\\eclipse-workspace\\aboutNew\\.venv\\Scripts\\python.exe",
            "src/main/resources/python/obtain-whatsNew.py", 
            keyword != null ? keyword : "",
            username != null ? username : ""
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
    
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
    
        String outputStr = output.toString().trim();
        System.out.println("*******************PYTHON OUTPUT:\n*************" + outputStr);
    
        if (!outputStr.startsWith("[") && !outputStr.startsWith("{")) {
            System.err.println("Python script did not return valid JSON: " + outputStr);
            return Collections.singletonList(Map.of("error", "Invalid JSON from Python"));
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
                if (o.has("error")) tweet.put("error", o.getString("error"));
                tweets.add(tweet);
            }
            return tweets;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonList(Map.of("error", e.getMessage()));
        }
    }
}
