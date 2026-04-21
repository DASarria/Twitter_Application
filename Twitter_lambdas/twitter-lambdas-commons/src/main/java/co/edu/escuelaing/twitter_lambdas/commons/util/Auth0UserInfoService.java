package co.edu.escuelaing.twitter_lambdas.commons.util;

import com.google.gson.Gson;
import co.edu.escuelaing.twitter_lambdas.commons.dto.Auth0UserInfoResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service to fetch user information from Auth0 userinfo endpoint
 */
public class Auth0UserInfoService {
    
    private final String issuerUri;
    private final Gson gson;
    private final HttpClient httpClient;

    public Auth0UserInfoService(String issuerUri) {
        this.issuerUri = normalizeIssuerUri(issuerUri);
        this.gson = new Gson();
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Fetch user info from Auth0 userinfo endpoint
     */
    public Auth0UserInfoResponse fetchUserInfo(String accessToken) throws Exception {
        String userInfoUrl = issuerUri + "userinfo";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(userInfoUrl))
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Auth0 userinfo request failed with status " + response.statusCode());
        }

        String responseBody = response.body();
        return gson.fromJson(responseBody, Auth0UserInfoResponse.class);
    }

    /**
     * Normalize issuer URI to ensure it ends with /
     */
    private String normalizeIssuerUri(String uri) {
        if (uri != null && !uri.endsWith("/")) {
            uri = uri + "/";
        }
        return uri;
    }

    /**
     * Close HTTP client
     */
    public void close() {
        // No-op for java.net.http.HttpClient
    }
}
