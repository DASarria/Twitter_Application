package co.edu.escuelaing.twitter.postsservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonSupport() {
    }

    public static <T> T fromJson(String body, Class<T> type) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Request body is required");
        }

        try {
            return MAPPER.readValue(body, type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Request body must be valid JSON");
        }
    }

    public static String toJson(Object body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }
}
