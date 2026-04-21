package co.edu.escuelaing.twitter_lambdas.commons.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to decode and extract information from JWT tokens.
 * Tokens are validated by API Gateway before reaching Lambda, so we only need to decode.
 */
public class JwtUtil {
    
    private static final String SCOPE_CLAIM = "scope";

    /**
     * Decode JWT token without verification (API Gateway does validation)
     */
    public static DecodedJWT decodeToken(String token) throws JWTDecodeException {
        return JWT.decode(token);
    }

    /**
     * Extract subject (user ID) from JWT
     */
    public static String getSubject(String token) throws JWTDecodeException {
        return decodeToken(token).getSubject();
    }

    /**
     * Extract claim value by name
     */
    public static String getClaimAsString(String token, String claimName) throws JWTDecodeException {
        DecodedJWT decodedJWT = decodeToken(token);
        Claim claim = decodedJWT.getClaim(claimName);
        return claim.isNull() ? null : claim.asString();
    }

    /**
     * Extract scopes from JWT as a list of strings
     */
    public static List<String> getScopes(String token) throws JWTDecodeException {
        DecodedJWT decodedJWT = decodeToken(token);
        Claim scopeClaim = decodedJWT.getClaim(SCOPE_CLAIM);
        if (scopeClaim != null && !scopeClaim.isNull()) {
            String scopes = scopeClaim.asString();
            return Arrays.asList(scopes.split(" "));
        }
        return Arrays.asList();
    }

    /**
     * Check if JWT contains a specific scope
     */
    public static boolean hasScope(String token, String requiredScope) throws JWTDecodeException {
        return getScopes(token).contains(requiredScope);
    }

    /**
     * Extract all claims from JWT
     */
    public static DecodedJWT getAllClaims(String token) throws JWTDecodeException {
        return decodeToken(token);
    }

    /**
     * Validate JWT format (basic checks)
     */
    public static boolean isValidTokenFormat(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }
            decodeToken(token);
            return true;
        } catch (JWTDecodeException e) {
            return false;
        }
    }
}
