package co.edu.escuelaing.twitter_lambdas.commons.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class Post {
    private String id;           // Partition Key - MongoDB ObjectId style
    private String authorSub;    // Sort Key - Auth0 subject identifier
    private String content;      // Post content
    private String authorName;   // Author's name
    private String authorPicture; // Author's picture URL
    private long createdAt;      // Timestamp of creation

    public Post() {}

    public Post(String id, String authorSub, String content, String authorName, 
                String authorPicture, long createdAt) {
        this.id = id;
        this.authorSub = authorSub;
        this.content = content;
        this.authorName = authorName;
        this.authorPicture = authorPicture;
        this.createdAt = createdAt;
    }

    @DynamoDbPartitionKey
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @DynamoDbSortKey
    public String getAuthorSub() { return authorSub; }
    public void setAuthorSub(String authorSub) { this.authorSub = authorSub; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorPicture() { return authorPicture; }
    public void setAuthorPicture(String authorPicture) { this.authorPicture = authorPicture; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
