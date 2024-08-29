package com.example.demo.domain.comment.domain.response;

import com.example.demo.domain.comment.domain.entity.Comment;
import com.example.demo.domain.study_project_board.domain.dto.vo.BoardCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class CommentPageResponse {
    private final int pageSize;
    private final int pageNum;
    private final int totalPage;
    private final String pageSort;
    private final List<MyCommentInfo> myCommentInfoList;

    public static CommentPageResponse from(Page<Comment> commentEntityPage, BoardCategory boardCategory) {
        List<MyCommentInfo> myCommentInfoList;
        switch (boardCategory) {
            case SEMINAR -> myCommentInfoList = commentEntityPage.stream()
                    .map(MyCommentInfo::fromSeminarComment)
                    .collect(Collectors.toList());
            case STUDY, PROJECT -> myCommentInfoList = commentEntityPage.stream()
                    .map(MyCommentInfo::fromStudyProjectComment)
                    .collect(Collectors.toList());
            default -> throw new IllegalArgumentException("게시판 종류에 해당하는 값이 아닙니다.");
        }

        return CommentPageResponse.builder()
                .pageSize(commentEntityPage.getSize())
                .pageNum(commentEntityPage.getNumber() + 1)
                .totalPage(commentEntityPage.getTotalPages())
                .pageSort(commentEntityPage.getSort().toString())
                .myCommentInfoList(myCommentInfoList)
                .build();
    }
}
