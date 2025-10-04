package aboutNew;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import aboutNew.controller.TweetController;
import aboutNew.db.SetupDB;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Find {
	
	public static void main(String[] args) {
        
        initializeDatabase();

        Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.http.defaultContentType = "application/json; charset=utf-8";
        })
        .get("/search", TweetController::search) //Breaking() js
        .get("/latest", TweetController::latest) //getNews() js
        .start(7070);

        startBackgroundJob();
    }

    public static void initializeDatabase() {
        System.out.println("Initializing Database");
        SetupDB.init();
        System.out.println("Database initialized successfully.");
    }

    private static void startBackgroundJob() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
                TweetController.fetchAndSaveDefaultSources();
        }, 0, 63, TimeUnit.MINUTES);    
    }

}
