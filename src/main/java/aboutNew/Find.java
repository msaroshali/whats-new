package aboutNew;
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
                String keyword = ctx.queryParam("keyword");
                List<Map<String, String>> tweets = Obtainer.getTweets(keyword);
                ctx.json(tweets);
            })
            .start(7070);
    }
	

}
