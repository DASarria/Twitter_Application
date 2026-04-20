package co.edu.escuelaing.twitter_application_monolith.dto;

import java.util.List;

public class MeResponse {
    private String sub;
    private String name;
    private String email;
    private List<String> scopes;

    public MeResponse(String sub, String name, String email, List<String> scopes) {
        this.sub = sub;
        this.name = name;
        this.email = email;
        this.scopes = scopes;
    }

    public String getSub() {
        return sub;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getScopes() {
        return scopes;
    }
}
