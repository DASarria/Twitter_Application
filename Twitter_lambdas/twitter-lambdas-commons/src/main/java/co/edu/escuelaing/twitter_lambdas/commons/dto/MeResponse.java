package co.edu.escuelaing.twitter_lambdas.commons.dto;

public class MeResponse {
    private String sub;
    private String name;
    private String email;
    private String picture;
    private String[] scopes;

    public MeResponse() {}

    public MeResponse(String sub, String name, String email, String picture, String[] scopes) {
        this.sub = sub;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.scopes = scopes;
    }

    // Getters and Setters
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public String[] getScopes() { return scopes; }
    public void setScopes(String[] scopes) { this.scopes = scopes; }
}
