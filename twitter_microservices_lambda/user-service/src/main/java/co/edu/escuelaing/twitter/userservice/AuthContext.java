package co.edu.escuelaing.twitter.userservice;

import java.util.List;

public record AuthContext(
        String sub,
        String name,
        String email,
        String picture,
        List<String> scopes) {
}
