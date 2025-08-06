package aboutNew;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Find {

	public static String world() {
		 String w =  "World";
		 return w;
	}
	
	public static void main(String[] args) {
        var app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        })
        .get("/search", ctx -> {
            try {    String keyword = ctx.queryParam("keyword");
            String username = ctx.queryParam("username");
            List<Map<String, String>> tweets = Obtainer.getTweets(keyword, username);
        // Always return an array (even if error)
        ctx.json(tweets != null ? tweets : Collections.emptyList());
    }  catch (Exception e) {
            e.printStackTrace(); // Detailed error in terminal
            ctx.status(500).json(Collections.singletonList(
                Map.of("error", "Internal Server Error: " + e.getMessage())
            ));
        }
        })
            .start(7070);
    }
	
    

}
