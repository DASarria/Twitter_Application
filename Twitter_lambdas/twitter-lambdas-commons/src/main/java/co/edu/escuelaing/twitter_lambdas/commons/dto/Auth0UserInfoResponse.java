package co.edu.escuelaing.twitter_lambdas.commons.dto;

public class Auth0UserInfoResponse {
    private String sub;
    private String name;
    private String email;
    private String picture;
    private String nickname;
    private String updated_at;

    public Auth0UserInfoResponse() {}

    // Getters and Setters
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
}
