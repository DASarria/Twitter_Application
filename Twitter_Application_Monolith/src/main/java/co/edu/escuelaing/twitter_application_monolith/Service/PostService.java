package co.edu.escuelaing.twitter_application_monolith.Service;

import co.edu.escuelaing.twitter_application_monolith.Entity.Post;
import co.edu.escuelaing.twitter_application_monolith.Repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(String authorSub, String authorName, String content) {
        Post post = new Post(authorSub, authorName, content, Instant.now());
        return postRepository.save(post);
    }

    public List<Post> getPublicStream() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }
}
