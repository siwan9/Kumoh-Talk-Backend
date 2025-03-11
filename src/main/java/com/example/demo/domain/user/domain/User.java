package com.example.demo.domain.user.domain;

import com.example.demo.domain.notification.domain.entity.NotificationUser;
import com.example.demo.domain.seminar_application.domain.SeminarApplication;
import com.example.demo.domain.user.domain.dto.request.UpdateUserInfoRequest;
import com.example.demo.domain.user.domain.vo.Role;
import com.example.demo.domain.user_addtional_info.domain.UserAdditionalInfo;
import com.example.demo.global.base.domain.BaseEntity;
import com.example.demo.global.oauth.user.OAuth2Provider;
import com.example.demo.infra.board.entity.Board;
import com.example.demo.infra.board.entity.Like;
import com.example.demo.infra.comment.entity.BoardComment;
import com.example.demo.infra.comment.entity.RecruitmentBoardComment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() where id=?")
@SQLRestriction(value = "deleted_at is NULL")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OAuth2Provider provider;

    @Column(nullable = false)
    private String providerId;

    @Column(unique = true)
    private String nickname;

    private String name;

    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_additional_info_id")
    private UserAdditionalInfo userAdditionalInfo;

    // TODO. 추후 user 와 newsletter 연동이 확정되면 추가
//    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "news_letter_id")
//    private Newsletter newsletter;


    @OneToMany(mappedBy = "user")
    private List<BoardComment> boardComments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<RecruitmentBoardComment> recruitmentBoardComments = new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeminarApplication> seminarApplications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationUser> notificationUsers = new ArrayList<>();

    @Builder
    public User(OAuth2Provider provider, String providerId, String nickname, Role role) {
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
        this.role = role;
    }

    public void setInitialInfo(String nickname, String name, String defaultImageUrl) {
        this.nickname = nickname;
        this.name = name;
        this.profileImageUrl = defaultImageUrl;
        this.role = Role.ROLE_USER;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void mapAdditionalInfo(UserAdditionalInfo userAdditionalInfo) {
        this.userAdditionalInfo = userAdditionalInfo;
    }

//    public void mapNewsletter(Newsletter newsletter) {
//        this.newsletter = newsletter;
//    }

    public void addSeminarApplications(SeminarApplication seminarApplication) {
        this.seminarApplications.add(seminarApplication);
    }

    public void updateUserRoleToActiveUser() {
        this.role = Role.ROLE_ACTIVE_USER;
    }

    public void updateUserRoleToSeminarWriter() {
        this.role = Role.ROLE_SEMINAR_WRITER;
    }

    public void changeProfileUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Boolean isAdmin() {
        return Role.ROLE_ADMIN.equals(this.role);
    }

    public void updateUserInfo(UpdateUserInfoRequest request) {
        this.nickname = request.nickname();
        this.name = request.name();
        this.profileImageUrl = request.profileImageUrl();
        this.role = request.role();
    }

//    public boolean hasNewsletter() {
//        return this.newsletter != null;
//    }

    public void setDefaultProfileUrl(String defaultImageUrl) {
        this.profileImageUrl = defaultImageUrl;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }

    public User(Long id) {
        this.id = id;
    }
}
