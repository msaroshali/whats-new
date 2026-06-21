package aboutNew;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rotator {
    private static final Logger logger = LoggerFactory.getLogger(Rotator.class);
    private static final List<String> apiKeys = new ArrayList<>();
    private static final List<String> models = new ArrayList<>();
    private static int currentKeyIndex = 0;
    private static int currentModelIndex = 0;

    static {
        try {
            Dotenv dotenv = Dotenv.load();
            addKey(dotenv.get("GEMINI_API_KEY"));
            addKey(dotenv.get("GEMINI_API_KEY1"));
            addKey(dotenv.get("GEMINI_API_KEY2"));
            addKey(dotenv.get("GEMINI_API_KEY3"));
            addKey(dotenv.get("GEMINI_API_KEY4"));
            addKey(dotenv.get("GEMINI_API_KEY5"));
            
            logger.info("[ApiKeyRotator] Loaded {} API keys from .env file.", apiKeys.size());
        } catch (Exception e) {
            logger.warn("[ApiKeyRotator] Could not load .env file: {}", e.getMessage());
        }

        models.addAll(Arrays.asList("gemini-3.5-flash", "gemini-3-flash-preview", "gemini-2.5-flash", "gemini-3.1-flash-lite", "gemini-2.5-flash-lite"));

    }

    private static void addKey(String key) {
        if (key != null && !key.trim().isEmpty()) {
            apiKeys.add(key);
        }
    }

    public static synchronized String getCurrentKey() {
        if (apiKeys.isEmpty()) {
            return null;
        }
        return apiKeys.get(currentKeyIndex);
    }

    public static synchronized String rotateKey() {
        if (apiKeys.size() <= 1) {
            logger.warn("[ApiKeyRotator] No fallback keys available to rotate.");
            return getCurrentKey();
        }
        int previousIndex = currentKeyIndex;
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size();
        logger.info("[ApiKeyRotator] Key rotated from index {} to index {}", previousIndex, currentKeyIndex);
        return getCurrentKey();
    }

    public static int getKeyCount() {
        return apiKeys.size();
    }
//-------------------------------Model Rotation Logic Placeholder------------------//
    public static String rotateModel() {
        int prevIndex = currentModelIndex;
        currentModelIndex = (currentModelIndex + 1) % models.size();
        logger.info("[ModelRotator] Model rotated from index {} to index {}", models.get(prevIndex), models.get(currentKeyIndex));

        return getModel();
    }
    public static String getModel() {
        // Implement model rotation logic here
        return models.get(currentModelIndex);
    }
        public static int getModelsCount() {
        return models.size();
    }
}
