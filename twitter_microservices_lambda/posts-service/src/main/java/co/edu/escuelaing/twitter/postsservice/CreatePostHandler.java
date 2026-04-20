package co.edu.escuelaing.twitter.postsservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreatePostHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final PostsRepository postsRepository = new PostsRepository();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        try {
            Map<String, String> claims = AuthClaimsExtractor.claims(request, List.of("write:posts"));
            CreatePostRequest body = JsonSupport.fromJson(request.getBody(), CreatePostRequest.class);
            String content = normalizeContent(body.getContent());

            String sub = claims.get("sub");
            String name = firstNonBlank(claims.get("name"), claims.get("nickname"), sub);

            Post post = new Post(
                    UUID.randomUUID().toString(),
                    sub,
                    name,
                    content,
                    Instant.now().toString());

            postsRepository.save(post);
            return ApiResponse.json(201, post);
        } catch (SecurityException ex) {
            return ApiResponse.error(403, "Forbidden", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("JWT")) {
                return ApiResponse.error(401, "Unauthorized", ex.getMessage());
            }
            return ApiResponse.error(400, "Validation failed", ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.error(500, "Internal server error", ex.getMessage());
        }
    }

    private String normalizeContent(String rawContent) {
        String content = rawContent == null ? "" : rawContent.trim();
        if (content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        if (content.length() > 140) {
            throw new IllegalArgumentException("content must be at most 140 characters");
        }
        return content;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
