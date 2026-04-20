package co.edu.escuelaing.twitter_application_monolith.Controller;

import co.edu.escuelaing.twitter_application_monolith.Service.Auth0UserInfoService;
import co.edu.escuelaing.twitter_application_monolith.dto.Auth0UserInfoResponse;
import co.edu.escuelaing.twitter_application_monolith.dto.MeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MeController {

    private final Auth0UserInfoService auth0UserInfoService;

    public MeController(Auth0UserInfoService auth0UserInfoService) {
        this.auth0UserInfoService = auth0UserInfoService;
    }

    @Operation(
            summary = "Get current authenticated user profile",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public MeResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String scopeClaim = jwt.getClaimAsString("scope");
        List<String> scopes = new ArrayList<>();
        if (scopeClaim != null && !scopeClaim.isBlank()) {
            scopes = Arrays.stream(scopeClaim.split(" ")).toList();
        }

        Auth0UserInfoResponse profile = auth0UserInfoService.fetchUserInfo(jwt.getTokenValue());

        return new MeResponse(
                profile != null && profile.getSub() != null ? profile.getSub() : jwt.getSubject(),
                profile != null ? profile.getName() : jwt.getClaimAsString("name"),
                profile != null ? profile.getEmail() : jwt.getClaimAsString("email"),
                profile != null ? profile.getPicture() : null,
                scopes
        );
    }
}
