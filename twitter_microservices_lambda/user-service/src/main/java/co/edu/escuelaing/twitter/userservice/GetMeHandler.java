package co.edu.escuelaing.twitter.userservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class GetMeHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        try {
            AuthContext auth = AuthClaimsExtractor.extract(request, List.of("read:profile"));
            Auth0UserInfo profile = fetchUserInfoIfPossible(request);

            MeResponse response = new MeResponse(
                    profile != null && profile.sub() != null ? profile.sub() : auth.sub(),
                    profile != null ? profile.name() : auth.name(),
                    profile != null ? profile.email() : auth.email(),
                    profile != null ? profile.picture() : auth.picture(),
                    auth.scopes());

            return ApiResponse.json(200, response);
        } catch (SecurityException ex) {
            return ApiResponse.error(403, "Forbidden", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(401, "Unauthorized", ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.error(500, "Internal server error", ex.getMessage());
        }
    }

    private Auth0UserInfo fetchUserInfoIfPossible(APIGatewayV2HTTPEvent request) {
        String token = extractBearerToken(request);
        if (token == null) {
            return null;
        }

        String issuer = requireEnv("AUTH0_ISSUER_URI");
        String normalizedIssuer = issuer.endsWith("/") ? issuer : issuer + "/";
        String userInfoUrl = normalizedIssuer + "userinfo";

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(userInfoUrl))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }

            return OBJECT_MAPPER.readValue(response.body(), Auth0UserInfo.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extractBearerToken(APIGatewayV2HTTPEvent request) {
        if (request == null || request.getHeaders() == null || request.getHeaders().isEmpty()) {
            return null;
        }

        Map<String, String> headers = request.getHeaders();
        String authorization = headers.getOrDefault("authorization", headers.get("Authorization"));
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        String token = authorization.substring("Bearer ".length()).trim();
        return token.isBlank() ? null : token;
    }

    private String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    private record Auth0UserInfo(
            String sub,
            String name,
            String email,
            String picture) {
    }
}
