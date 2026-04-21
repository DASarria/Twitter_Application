package co.edu.escuelaing.twitter_lambdas.commons.util;

import co.edu.escuelaing.twitter_lambdas.commons.entity.Post;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Post entity with DynamoDB operations
 */
public class PostRepository {

    private final DynamoDbTable<Post> table;
    private static final String DEFAULT_TABLE_NAME = "posts";

    public PostRepository(DynamoDbClient dynamoDbClient) {
        this(dynamoDbClient, DEFAULT_TABLE_NAME);
    }

    public PostRepository(DynamoDbClient dynamoDbClient, String tableName) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(Post.class));
    }

    /**
     * Save a new post
     */
    public Post save(Post post) {
        // Generate ID if not provided
        if (post.getId() == null || post.getId().isEmpty()) {
            post.setId(UUID.randomUUID().toString());
        }
        table.putItem(post);
        return post;
    }

    /**
     * Find post by ID and authorSub
     */
    public Post findById(String id, String authorSub) {
        Key key = Key.builder()
                .partitionValue(id)
                .sortValue(authorSub)
                .build();
        return table.getItem(key);
    }

    /**
     * Get all posts by author (using authorSub as sort key)
     */
    public List<Post> findByAuthorSub(String authorSub) {
        List<Post> posts = findAll();
        posts.removeIf(post -> !authorSub.equals(post.getAuthorSub()));
        return posts;
    }

    /**
     * Get all posts (scan operation - use with caution in production)
     */
    public List<Post> findAll() {
        PageIterable<Post> pages = table.scan();
        List<Post> posts = new ArrayList<>();
        
        for (Page<Post> page : pages) {
            posts.addAll(page.items());
        }
        
        return posts;
    }

    /**
     * Get all posts sorted by creation date (descending)
     */
    public List<Post> findAllSortedByCreatedAt() {
        List<Post> allPosts = findAll();
        allPosts.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
        return allPosts;
    }

    /**
     * Delete post by ID and authorSub
     */
    public void delete(String id, String authorSub) {
        Key key = Key.builder()
                .partitionValue(id)
                .sortValue(authorSub)
                .build();
        table.deleteItem(key);
    }
}
