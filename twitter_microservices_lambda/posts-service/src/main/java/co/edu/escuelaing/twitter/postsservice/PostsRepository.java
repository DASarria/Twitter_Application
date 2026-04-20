package co.edu.escuelaing.twitter.postsservice;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

public class PostsRepository {

    private static final String STREAM_PK = "STREAM#GLOBAL";
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public PostsRepository() {
        this(DynamoDbClient.create(), requireEnv("POSTS_TABLE_NAME"));
    }

    public PostsRepository(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    public Post save(Post post) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("partitionKey", AttributeValue.builder().s(STREAM_PK).build());
        item.put("sortKey", AttributeValue.builder().s("POST#" + post.createdAt() + "#" + post.id()).build());
        item.put("id", AttributeValue.builder().s(post.id()).build());
        item.put("authorSub", AttributeValue.builder().s(post.authorSub()).build());
        item.put("authorName", AttributeValue.builder().s(post.authorName()).build());
        item.put("content", AttributeValue.builder().s(post.content()).build());
        item.put("createdAt", AttributeValue.builder().s(post.createdAt()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
        return post;
    }

    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }
}
