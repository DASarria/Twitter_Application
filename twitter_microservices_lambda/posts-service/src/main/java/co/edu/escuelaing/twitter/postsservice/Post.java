package co.edu.escuelaing.twitter.postsservice;

public record Post(
        String id,
        String authorSub,
        String authorName,
        String content,
        String createdAt) {
}
