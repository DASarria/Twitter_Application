package co.edu.escuelaing.twitter_application_monolith.dto;

import java.time.Instant;

public class PostResponse {
    private String id;
    private String authorSub;
    private String authorName;
    private String content;
    private Instant createdAt;

    public PostResponse() {
    }

    public PostResponse(String id, String authorSub, String authorName, String content, Instant createdAt) {
        this.id = id;
        this.authorSub = authorSub;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getAuthorSub() {
        return authorSub;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
