package ru.myproject.itq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "approval_registry")
public class ApprovalRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ToString.Include
    private Long approvalId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, updatable = false, unique = true)
    private Document document;

    @Column(name = "document_number", nullable = false)
    @ToString.Include
    private Long documentNumber;

    @Column(name = "approved_by", nullable = false)
    @ToString.Include
    private String approvedBy;

    @Column(name = "approved_at", nullable = false)
    @CreationTimestamp
    @ToString.Include
    private Instant approvedAt;

    @ToString.Include(name = "documentId")
    private Long documentIdToString(){
        return document != null ? document.getDocumentId() : null;
    }

    @Builder
    public ApprovalRegistry(Document documentId, Long documentNumber, String approvedBy) {
        this.document = documentId;
        this.documentNumber = documentNumber;
        this.approvedBy = approvedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ApprovalRegistry that = (ApprovalRegistry) o;
        return approvalId != null && approvalId.equals(that.approvalId);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
