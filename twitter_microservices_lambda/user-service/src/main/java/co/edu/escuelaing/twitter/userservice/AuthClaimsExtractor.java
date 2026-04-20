package co.edu.escuelaing.twitter.userservice;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AuthClaimsExtractor {

    private AuthClaimsExtractor() {
    }

    public static AuthContext extract(APIGatewayV2HTTPEvent event, List<String> requiredScopes) {
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

        Set<String> scopeSet = new HashSet<>();
        String scopeClaim = claims.get("scope");
        if (scopeClaim != null && !scopeClaim.isBlank()) {
            for (String scope : scopeClaim.split(" ")) {
                if (!scope.isBlank()) {
                    scopeSet.add(scope.trim());
                }
            }
        }

        List<String> scopes = new ArrayList<>(scopeSet);
        List<String> missingScopes = requiredScopes.stream().filter(scope -> !scopeSet.contains(scope)).toList();
        if (!missingScopes.isEmpty()) {
            throw new SecurityException("Missing required scopes: " + String.join(", ", missingScopes));
        }

        String name = firstNonBlank(claims.get("name"), claims.get("nickname"), sub);
        return new AuthContext(sub, name, claims.get("email"), claims.get("picture"), scopes);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
