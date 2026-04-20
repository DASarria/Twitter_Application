package co.edu.escuelaing.twitter.streamservice;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.HashMap;
import java.util.Map;

public final class ApiResponse {

    private ApiResponse() {
    }

    public static APIGatewayV2HTTPResponse json(int statusCode, Object body) {
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(statusCode)
                .withHeaders(defaultHeaders())
                .withBody(JsonSupport.toJson(body))
                .build();
    }

    public static APIGatewayV2HTTPResponse error(int statusCode, String message, String detail) {
        Map<String, String> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("detail", detail);
        return json(statusCode, payload);
    }

    private static Map<String, String> defaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Authorization,Content-Type,Accept,Origin");
        headers.put("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        return headers;
    }
}
