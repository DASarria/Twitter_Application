package co.edu.escuelaing.twitter.streamservice;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.ArrayList;
import java.util.List;
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

    public List<Post> fetchGlobalStream(int limit) {
        int safeLimit = Math.max(1, Math.min(100, limit));

        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("partitionKey = :pk")
                .expressionAttributeValues(Map.of(":pk", AttributeValue.builder().s(STREAM_PK).build()))
                .scanIndexForward(false)
                .limit(safeLimit)
                .build();

        QueryResponse response = dynamoDbClient.query(request);

        List<Post> posts = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            posts.add(new Post(
                    read(item, "id"),
                    read(item, "authorSub"),
                    read(item, "authorName"),
                    read(item, "content"),
                    read(item, "createdAt")));
        }
        return posts;
    }

    private String read(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null || value.s() == null ? "" : value.s();
    }

    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }
}
