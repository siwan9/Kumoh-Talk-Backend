package com.example.demo.domain.comment.domain.dto.response;

import com.example.demo.domain.comment.domain.entity.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class MyCommentResponse {

    private MyCommentInfoResponse comment;
    private CommentBoardInfoResponse board;

    public static MyCommentResponse fromSeminarComment(Comment commentEntity) {
        return MyCommentResponse.builder()
                .comment(MyCommentInfoResponse.builder()
                        .id(commentEntity.getId())
                        .content(commentEntity.getContent())
                        .createdAt(commentEntity.getCreatedAt())
                        .updatedAt(commentEntity.getUpdatedAt())
                        .build())
                .board(CommentBoardInfoResponse.builder()
                        .id(commentEntity.getBoard().getId())
                        .title(commentEntity.getBoard().getTitle())
                        .createdAt(commentEntity.getBoard().getCreatedAt())
                        .updatedAt(commentEntity.getBoard().getUpdatedAt())
                        .build())
                .build();
    }

    public static MyCommentResponse fromRecruitmentComment(com.example.demo.domain.comment.domain.entity.Comment commentEntity) {
        return MyCommentResponse.builder()
                .comment(MyCommentInfoResponse.builder()
                        .id(commentEntity.getId())
                        .content(commentEntity.getContent())
                        .createdAt(commentEntity.getCreatedAt())
                        .updatedAt(commentEntity.getUpdatedAt())
                        .build())
                .board(CommentBoardInfoResponse.builder()
                        .id(commentEntity.getRecruitmentBoard().getId())
                        .createdAt(commentEntity.getRecruitmentBoard().getCreatedAt())
                        .updatedAt(commentEntity.getRecruitmentBoard().getUpdatedAt())
                        .build())
                .build();
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class MyCommentInfoResponse {
        private Long id;
        private String content;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class CommentBoardInfoResponse {
        private Long id;
        private String title;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
    }
}