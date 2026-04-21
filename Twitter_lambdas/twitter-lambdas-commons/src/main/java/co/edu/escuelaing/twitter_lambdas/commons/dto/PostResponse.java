package co.edu.escuelaing.twitter_lambdas.commons.dto;

import com.google.gson.annotations.SerializedName;

public class PostResponse {
    @SerializedName("_id")
    private String id;
    
    private String content;
    
    @SerializedName("authorSub")
    private String authorSub;
    
    @SerializedName("authorName")
    private String authorName;
    
    @SerializedName("authorPicture")
    private String authorPicture;
    
    @SerializedName("createdAt")
    private long createdAt;

    public PostResponse() {}

    public PostResponse(String id, String content, String authorSub, String authorName, 
                       String authorPicture, long createdAt) {
        this.id = id;
        this.content = content;
        this.authorSub = authorSub;
        this.authorName = authorName;
        this.authorPicture = authorPicture;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorSub() { return authorSub; }
    public void setAuthorSub(String authorSub) { this.authorSub = authorSub; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorPicture() { return authorPicture; }
    public void setAuthorPicture(String authorPicture) { this.authorPicture = authorPicture; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
