package co.edu.escuelaing.twitter_application_monolith.Controller;

import co.edu.escuelaing.twitter_application_monolith.Entity.Post;
import co.edu.escuelaing.twitter_application_monolith.Service.PostService;
import co.edu.escuelaing.twitter_application_monolith.Service.Auth0UserInfoService;
import co.edu.escuelaing.twitter_application_monolith.dto.Auth0UserInfoResponse;
import co.edu.escuelaing.twitter_application_monolith.dto.CreatePostRequest;
import co.edu.escuelaing.twitter_application_monolith.dto.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;
    private final Auth0UserInfoService auth0UserInfoService;

    public PostController(PostService postService, Auth0UserInfoService auth0UserInfoService) {
        this.postService = postService;
        this.auth0UserInfoService = auth0UserInfoService;
    }

    @Operation(summary = "Get public stream of posts")
    @GetMapping({"/posts", "/stream"})
    public List<PostResponse> getPublicPosts() {
        return postService.getPublicStream().stream().map(this::toResponse).toList();
    }

    @Operation(
            summary = "Create a new post",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/posts")
        public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal Jwt jwt
        ) {

        Auth0UserInfoResponse profile = auth0UserInfoService.fetchUserInfo(jwt.getTokenValue());
        String authorSub = profile.getSub() != null ? profile.getSub() : jwt.getSubject();
        String authorName = profile.getName() != null ? profile.getName() : jwt.getClaimAsString("name");
        if (authorName == null || authorName.isBlank()) {
            authorName = jwt.getClaimAsString("nickname");
        }
        if (authorName == null || authorName.isBlank()) {
            authorName = authorSub;
        }

        Post createdPost = postService.createPost(authorSub, authorName, request.getContent().trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(createdPost));
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthorSub(),
                post.getAuthorName(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
