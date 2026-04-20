package co.edu.escuelaing.twitter_application_monolith.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    @Indexed
    private String authorSub;

    private String authorName;

    private String content;

    @Indexed
    private Instant createdAt;

    public Post() {
    }

    public Post(String authorSub, String authorName, String content, Instant createdAt) {
        this.authorSub = authorSub;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorSub() {
        return authorSub;
    }

    public void setAuthorSub(String authorSub) {
        this.authorSub = authorSub;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
