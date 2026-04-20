package co.edu.escuelaing.twitter.streamservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.Map;

public class GetStreamHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final PostsRepository postsRepository = new PostsRepository();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        try {
            int limit = extractLimit(request.getQueryStringParameters());
            return ApiResponse.json(200, postsRepository.fetchGlobalStream(limit));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(400, "Bad request", ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.error(500, "Internal server error", ex.getMessage());
        }
    }

    private int extractLimit(Map<String, String> queryParams) {
        if (queryParams == null || !queryParams.containsKey("limit")) {
            return 50;
        }

        try {
            return Integer.parseInt(queryParams.get("limit"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("limit query param must be numeric");
        }
    }
}
