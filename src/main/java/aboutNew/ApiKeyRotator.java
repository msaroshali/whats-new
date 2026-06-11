package aboutNew;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class ApiKeyRotator {
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyRotator.class);
    private static final List<String> apiKeys = new ArrayList<>();
    private static int currentKeyIndex = 0;

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
}
