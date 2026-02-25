package ru.myproject.itq.config;

import org.springframework.data.jpa.domain.Specification;
import ru.myproject.itq.entity.Document;
import ru.myproject.itq.enums.DocumentStatus;

import java.time.Instant;

public class DocumentSpecifications {
    private DocumentSpecifications() {}

    public static Specification<Document> hasStatus(DocumentStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Document> hasAuthor(String author) {
        return (root, query, cb) -> (author == null || author.isBlank())
                ? cb.conjunction()
                : cb.equal(root.get("author"), author);
    }

    public static Specification<Document> createdFrom(Instant from) {
        return (root, query, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Document> createdTo(Instant to) {
        return (root, query, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
