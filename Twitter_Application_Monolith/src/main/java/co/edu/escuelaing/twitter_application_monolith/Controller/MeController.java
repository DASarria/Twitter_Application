package co.edu.escuelaing.twitter_application_monolith.Controller;

import co.edu.escuelaing.twitter_application_monolith.dto.MeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Operation(
            summary = "Get current authenticated user profile",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public MeResponse getCurrentUser(Jwt jwt) {
        String scopeClaim = jwt.getClaimAsString("scope");
        List<String> scopes = new ArrayList<>();
        if (scopeClaim != null && !scopeClaim.isBlank()) {
            scopes = Arrays.stream(scopeClaim.split(" ")).toList();
        }

        return new MeResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("email"),
                scopes
        );
    }
}
