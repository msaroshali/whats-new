package aboutNew;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-2:embedContent";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static float[] getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        int attempts = Rotator.getKeyCount();
        for (int attempt = 0; attempt < attempts; attempt++) {
            String apiKey = Rotator.getCurrentKey();
            if (apiKey == null) {
                logger.error("[EmbeddingService] No API keys loaded in ApiKeyRotator!");
                return null;
            }

            try {
                // Build request JSON
                JSONObject part = new JSONObject().put("text", text);
                JSONObject contentObj = new JSONObject().put("parts", new JSONArray().put(part));
                JSONObject requestBody = new JSONObject()
                        .put("model", "models/gemini-embedding-2")
                        .put("content", contentObj);

                String requestUrl = API_URL + "?key=" + apiKey;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(requestUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    JSONObject embeddingObj = jsonResponse.getJSONObject("embedding");
                    JSONArray valuesArray = embeddingObj.getJSONArray("values");

                    float[] vector = new float[valuesArray.length()];
                    for (int i = 0; i < valuesArray.length(); i++) {
                        vector[i] = (float) valuesArray.getDouble(i);
                    }
                    return vector;
                } else if (response.statusCode() == 429 || response.statusCode() == 403) {
                    logger.warn("[EmbeddingService] API Key exhausted (Status: {}). Rotating key and retrying...", response.statusCode());
                    Rotator.rotateKey();
                } else {
                    logger.error("[EmbeddingService] Gemini Embedding API returned status code {}: {}", response.statusCode(), response.body());
                    return null;
                }

            } catch (Exception e) {
                logger.error("[EmbeddingService] Error fetching embedding from Gemini API:", e);
                return null;
            }
        }
        logger.error("[EmbeddingService] All API keys have been exhausted for this request.");
        return null;
    }
}
