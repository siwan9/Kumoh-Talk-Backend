package com.example.demo.domain.newsletter.domain;

import com.example.demo.domain.newsletter.domain.dto.request.NewsletterSubscribeRequest;
import com.example.demo.domain.newsletter.domain.dto.request.NewsletterUpdateEmailRequest;
import com.example.demo.domain.newsletter.domain.dto.request.NewsletterUpdateNotifyRequest;
import com.example.demo.global.base.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "newsletters")
public class Newsletter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private Boolean isSeminarContentUpdated;
    @Column(nullable = false)
    private Boolean isStudyUpdated;
    @Column(nullable = false)
    private Boolean isProjectUpdated;

    @Builder
    public Newsletter(String email, Boolean isSeminarContentUpdated, Boolean isStudyUpdated, Boolean isProjectUpdated) {
        this.email = email;
        this.isSeminarContentUpdated = isSeminarContentUpdated;
        this.isStudyUpdated = isStudyUpdated;
        this.isProjectUpdated = isProjectUpdated;
    }

    public static Newsletter from(NewsletterSubscribeRequest request) {
        return Newsletter.builder()
                .email(request.email())
                .isSeminarContentUpdated(request.isSeminarContentUpdated())
                .isStudyUpdated(request.isStudyUpdated())
                .isProjectUpdated(request.isProjectUpdated())
                .build();
    }

    public void updateNewsletterEmail(@Valid NewsletterUpdateEmailRequest request) {
        this.email = request.email();
    }

    public void updateNewsletterNotify(@Valid NewsletterUpdateNotifyRequest request) {
        this.isSeminarContentUpdated = request.isSeminarContentUpdated();
        this.isStudyUpdated = request.isStudyUpdated();
        this.isProjectUpdated = request.isProjectUpdated();
    }
}
