package aboutNew;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class Obtainer {
public static List<Map<String, String>> getTweets(String keyword) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("C:\\Users\\PC 3\\eclipse-workspace\\aboutNew\\.venv\\Scripts\\python.exe",
        "src/main/resources/python/obtain-whatsNew.py",keyword);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        System.out.println("*******************PYTHON OUTPUT:\n*************" + output.toString());
        JSONArray arr = new JSONArray(output.toString());
        List<Map<String, String>> tweets = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Map<String, String> tweet = new HashMap<>();
            tweet.put("date", o.getString("date"));
            tweet.put("content", o.getString("content"));
            tweets.add(tweet);
        }
        return tweets;
    }
}
