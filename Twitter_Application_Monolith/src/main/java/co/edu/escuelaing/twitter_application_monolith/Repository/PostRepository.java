package co.edu.escuelaing.twitter_application_monolith.Repository;

import co.edu.escuelaing.twitter_application_monolith.Entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findAllByOrderByCreatedAtDesc();
}
