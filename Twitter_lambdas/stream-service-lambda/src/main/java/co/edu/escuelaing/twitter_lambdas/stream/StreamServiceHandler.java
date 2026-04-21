package co.edu.escuelaing.twitter_lambdas.stream;

import co.edu.escuelaing.twitter_lambdas.commons.dto.PostResponse;
import co.edu.escuelaing.twitter_lambdas.commons.entity.Post;
import co.edu.escuelaing.twitter_lambdas.commons.util.PostRepository;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StreamServiceHandler implements RequestStreamHandler {

    private static final Gson GSON = new Gson();
    private static final String DEFAULT_TABLE_NAME = "posts";

    private static final DynamoDbClient DYNAMO_DB_CLIENT = DynamoDbClient.builder()
            .region(Region.of(getEnvOrDefaultStatic("AWS_REGION", "us-east-1")))
            .build();
    private static final PostRepository POST_REPOSITORY = new PostRepository(
        DYNAMO_DB_CLIENT,
        getEnvOrDefaultStatic("DYNAMODB_TABLE_NAME", DEFAULT_TABLE_NAME)
    );

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        JsonObject event = GSON.fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), JsonObject.class);
        Map<String, Object> response;

        try {
            String method = getHttpMethod(event);
            String path = getPath(event);
            if ("OPTIONS".equalsIgnoreCase(method)) {
                response = successResponse(200, Map.of("ok", true));
            } else if (!"GET".equalsIgnoreCase(method) || !isSupportedPath(path)) {
                response = errorResponse(404, "Not Found");
            } else {
                response = processStream();
            }
        } catch (Exception e) {
            context.getLogger().log("stream-service error: " + e.getMessage());
            response = errorResponse(500, "Internal Server Error");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            GSON.toJson(response, writer);
        }
    }

    private Map<String, Object> processStream() {
        List<Post> posts = POST_REPOSITORY.findAllSortedByCreatedAt();
        List<PostResponse> payload = posts.stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getContent(),
                        post.getAuthorSub(),
                        post.getAuthorName(),
                        post.getAuthorPicture(),
                        post.getCreatedAt()))
                .toList();

        return successResponse(200, payload);
    }

    private boolean isSupportedPath(String path) {
        return "/api/posts".equals(path) || "/api/stream".equals(path);
    }

    private String getHttpMethod(JsonObject event) {
        if (!event.has("requestContext")) {
            return "";
        }
        return event.getAsJsonObject("requestContext")
                .getAsJsonObject("http")
                .get("method")
                .getAsString();
    }

    private String getPath(JsonObject event) {
        String path = "";
        if (event.has("rawPath")) {
            path = event.get("rawPath").getAsString();
        }
        if (event.has("requestContext") && event.getAsJsonObject("requestContext").has("http")) {
            JsonObject http = event.getAsJsonObject("requestContext").getAsJsonObject("http");
            if (http.has("path")) {
                path = http.get("path").getAsString();
            }
        }
        return normalizePath(path, event);
    }

    private String normalizePath(String path, JsonObject event) {
        if (path == null || path.isBlank()) {
            return "";
        }

        String normalized = path;
        if (event.has("requestContext") && event.getAsJsonObject("requestContext").has("stage")) {
            String stage = event.getAsJsonObject("requestContext").get("stage").getAsString();
            if (stage != null && !stage.isBlank() && !"$default".equals(stage)) {
                String stagePrefix = "/" + stage;
                if (normalized.equals(stagePrefix)) {
                    return "/";
                }
                if (normalized.startsWith(stagePrefix + "/")) {
                    normalized = normalized.substring(stagePrefix.length());
                }
            }
        }
        return normalized;
    }

    private static String getEnvOrDefaultStatic(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private Map<String, Object> successResponse(int statusCode, Object body) {
        return baseResponse(statusCode, GSON.toJson(body));
    }

    private Map<String, Object> errorResponse(int statusCode, String message) {
        Map<String, String> error = Map.of("error", message);
        return baseResponse(statusCode, GSON.toJson(error));
    }

    private Map<String, Object> baseResponse(int statusCode, String bodyJson) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,Authorization");
        headers.put("Access-Control-Allow-Methods", "GET,POST,OPTIONS");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", statusCode);
        response.put("headers", headers);
        response.put("isBase64Encoded", false);
        response.put("body", bodyJson);
        return response;
    }
}
