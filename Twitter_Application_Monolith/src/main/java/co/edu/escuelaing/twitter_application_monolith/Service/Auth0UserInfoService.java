package co.edu.escuelaing.twitter_application_monolith.Service;

import co.edu.escuelaing.twitter_application_monolith.dto.Auth0UserInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class Auth0UserInfoService {

    private final RestClient restClient;
    private final String userInfoUrl;

    public Auth0UserInfoService(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        this.restClient = RestClient.builder().build();
        this.userInfoUrl = normalizeIssuer(issuerUri) + "userinfo";
    }

    public Auth0UserInfoResponse fetchUserInfo(String accessToken) {
        return restClient.get()
                .uri(userInfoUrl)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(Auth0UserInfoResponse.class);
    }

    private String normalizeIssuer(String rawIssuer) {
        if (rawIssuer == null) {
            return "";
        }
        return rawIssuer.endsWith("/") ? rawIssuer : rawIssuer + "/";
    }
}
