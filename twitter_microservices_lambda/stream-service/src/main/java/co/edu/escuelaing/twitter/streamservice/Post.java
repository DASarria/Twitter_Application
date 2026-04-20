package co.edu.escuelaing.twitter.streamservice;

public record Post(
        String id,
        String authorSub,
        String authorName,
        String content,
        String createdAt) {
}
