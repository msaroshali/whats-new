package aboutNew;

import aboutNew.controller.TweetController;
import aboutNew.db.SetupDB;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Find {
	
	public static void main(String[] args) {
        
        initializeDatabase();

        Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        })
        .get("/search", TweetController::search)
        .start(7070);
    }

    public static void initializeDatabase() {
        System.out.println("Initializing Database");
        SetupDB.init();
        System.out.println("Database initialized successfully.");
    }
}
