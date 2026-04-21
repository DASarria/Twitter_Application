package co.edu.escuelaing.twitter_lambdas.commons.dto;

import com.google.gson.annotations.SerializedName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePostRequest {
    @NotBlank(message = "Content cannot be empty")
    @Size(min = 1, max = 140, message = "Content must be between 1 and 140 characters")
    private String content;

    public CreatePostRequest() {}

    public CreatePostRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
