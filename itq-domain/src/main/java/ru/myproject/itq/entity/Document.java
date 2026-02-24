package ru.myproject.itq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.myproject.itq.enums.DocumentStatus;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "documents")
public class Document {
    @Id
    @SequenceGenerator(
            name = "documents_id_seq_gen",
            sequenceName = "documents_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "documents_id_seq_gen")
    @Column(name = "id")
    @ToString.Include
    private Long documentId;

    @Column(name = "number", nullable = false, unique = true, insertable = false, updatable = false)
    @ToString.Include
    private Long number;

    @Column(name = "author", nullable = false)
    @ToString.Include
    private String author;

    @Column(name = "title", nullable = false)
    @ToString.Include
    private String title;

    @Column(name = "status", nullable = false)
    @ToString.Include
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    @ToString.Include
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    @ToString.Include
    private Instant updatedAt;

    @Column(name = "version", nullable = false)
    @Version
    private long version;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<DocumentHistory> documentHistories;

    @OneToOne(mappedBy = "document")
    @ToString.Exclude
    private ApprovalRegistry approvalRegistry;

    @Builder
    public Document(String author, String title, DocumentStatus status) {
        this.author = author;
        this.title = title;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;

        Document document = (Document) o;
        return documentId != null && documentId.equals(document.documentId);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
