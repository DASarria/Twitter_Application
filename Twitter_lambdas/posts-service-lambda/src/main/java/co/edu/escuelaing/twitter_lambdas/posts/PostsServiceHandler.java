package co.edu.escuelaing.twitter_lambdas.posts;

import co.edu.escuelaing.twitter_lambdas.commons.dto.Auth0UserInfoResponse;
import co.edu.escuelaing.twitter_lambdas.commons.dto.CreatePostRequest;
import co.edu.escuelaing.twitter_lambdas.commons.dto.PostResponse;
import co.edu.escuelaing.twitter_lambdas.commons.entity.Post;
import co.edu.escuelaing.twitter_lambdas.commons.util.Auth0UserInfoService;
import co.edu.escuelaing.twitter_lambdas.commons.util.JwtUtil;
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
import java.util.Map;

public class PostsServiceHandler implements RequestStreamHandler {

    private static final Gson GSON = new Gson();
    private static final String DEFAULT_ISSUER = "https://auth0.com/";
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
            } else if (!"POST".equalsIgnoreCase(method) || !"/api/posts".equals(path)) {
                response = errorResponse(404, "Not Found");
            } else {
                response = processCreatePost(event);
            }
        } catch (Exception e) {
            context.getLogger().log("posts-service error: " + e.getMessage());
            response = errorResponse(500, "Internal Server Error");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            GSON.toJson(response, writer);
        }
    }

    private Map<String, Object> processCreatePost(JsonObject event) throws Exception {
        String token = extractBearerToken(event);
        if (token == null || token.isBlank() || !JwtUtil.isValidTokenFormat(token)) {
            return errorResponse(401, "Unauthorized");
        }

        String requestBody = event.has("body") && !event.get("body").isJsonNull()
                ? event.get("body").getAsString()
                : null;
        if (requestBody == null || requestBody.isBlank()) {
            return errorResponse(400, "Request body is required");
        }

        CreatePostRequest createPostRequest = GSON.fromJson(requestBody, CreatePostRequest.class);
        if (createPostRequest == null || createPostRequest.getContent() == null) {
            return errorResponse(400, "Content is required");
        }

        String content = createPostRequest.getContent().trim();
        if (content.isEmpty() || content.length() > 140) {
            return errorResponse(400, "Content must be between 1 and 140 characters");
        }

        String issuer = getEnvOrDefault("AUTH0_ISSUER_URI", DEFAULT_ISSUER);
        Auth0UserInfoService userInfoService = new Auth0UserInfoService(issuer);
        Auth0UserInfoResponse userInfo = userInfoService.fetchUserInfo(token);

        Post post = new Post();
        post.setAuthorSub(userInfo.getSub());
        post.setAuthorName(userInfo.getName());
        post.setAuthorPicture(userInfo.getPicture());
        post.setContent(content);
        post.setCreatedAt(System.currentTimeMillis());

        Post saved = POST_REPOSITORY.save(post);

        PostResponse response = new PostResponse(
                saved.getId(),
                saved.getContent(),
                saved.getAuthorSub(),
                saved.getAuthorName(),
                saved.getAuthorPicture(),
                saved.getCreatedAt()
        );

        return successResponse(201, response);
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

    private String extractBearerToken(JsonObject event) {
        if (!event.has("headers") || !event.get("headers").isJsonObject()) {
            return null;
        }
        JsonObject headers = event.getAsJsonObject("headers");
        String authHeader = null;

        if (headers.has("authorization")) {
            authHeader = headers.get("authorization").getAsString();
        } else if (headers.has("Authorization")) {
            authHeader = headers.get("Authorization").getAsString();
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring("Bearer ".length()).trim();
    }

    private String getEnvOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
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
