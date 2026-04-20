package co.edu.escuelaing.twitter.postsservice;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AuthClaimsExtractor {

    private AuthClaimsExtractor() {
    }

    public static Map<String, String> claims(APIGatewayV2HTTPEvent event, List<String> requiredScopes) {
        if (event == null || event.getRequestContext() == null || event.getRequestContext().getAuthorizer() == null) {
            throw new IllegalArgumentException("Missing JWT claims. Ensure API Gateway authorizer is enabled.");
        }

        APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT jwt = event.getRequestContext().getAuthorizer().getJwt();
        if (jwt == null || jwt.getClaims() == null || jwt.getClaims().isEmpty()) {
            throw new IllegalArgumentException("Missing JWT claims. Ensure API Gateway authorizer is enabled.");
        }

        Map<String, String> claims = jwt.getClaims();
        String sub = claims.get("sub");
        if (sub == null || sub.isBlank()) {
            throw new SecurityException("JWT subject is missing");
        }

        Set<String> scopes = new HashSet<>();
        String scopeClaim = claims.get("scope");
        if (scopeClaim != null && !scopeClaim.isBlank()) {
            for (String scope : scopeClaim.split(" ")) {
                if (!scope.isBlank()) {
                    scopes.add(scope.trim());
                }
            }
        }

        List<String> missingScopes = requiredScopes.stream().filter(scope -> !scopes.contains(scope)).toList();
        if (!missingScopes.isEmpty()) {
            throw new SecurityException("Missing required scopes: " + String.join(", ", missingScopes));
        }

        return claims;
    }
}
