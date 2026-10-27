package aboutNew;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import aboutNew.controller.TweetController;
import aboutNew.db.SetupDB;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Find {
    private static final Logger logger = LoggerFactory.getLogger(Find.class);
	
	public static void main(String[] args) {
        
        initializeDatabase();
        aboutNew.dao.TweetDAO.startBackgroundEmbeddingMigration();

        Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.http.defaultContentType = "application/json; charset=utf-8";
        })
        .get("/search", TweetController::search) //Breaking() js calls this
        .get("/latest", TweetController::latest) //getNews() js calls this
        .get("/ai-search", TweetController::aiSearch) //askAI() js calls this
        .get("/summary", TweetController::timeframeSummary)
        .get("/ticker", TweetController::ticker)
        .start(7070);

        startBackgroundJob();
    }

    public static void initializeDatabase() {
        logger.info("Initializing Database");
        SetupDB.init();
        logger.info("Database initialized successfully.");
    }

    private static void startBackgroundJob() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
                TweetController.fetchAndSaveDefaultSources();
        }, 1, 63, TimeUnit.MINUTES);    
    }

}
