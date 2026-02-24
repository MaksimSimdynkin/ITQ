package ru.myproject.itq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import ru.myproject.itq.enums.DocumentHistoryAction;

import java.time.Instant;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "document_history")
public class DocumentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long documentHistoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, updatable = false)
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    @ToString.Include
    private DocumentHistoryAction action;

    @Column(name = "actor", nullable = false)
    @ToString.Include
    private String actor;

    @Column(name = "comment")
    @ToString.Include
    private String comment;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    @ToString.Include
    private Instant createdAt;

    @ToString.Include(name = "documentId")
    private Long documentIdToString(){
        return document != null ? document.getDocumentId() : null;
    }

    @Builder
    public DocumentHistory(String comment, String actor, DocumentHistoryAction action, Document documentId) {
        this.comment = comment;
        this.actor = actor;
        this.action = action;
        this.document = documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DocumentHistory that = (DocumentHistory) o;
        return documentHistoryId != null && documentHistoryId.equals(that.documentHistoryId);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
