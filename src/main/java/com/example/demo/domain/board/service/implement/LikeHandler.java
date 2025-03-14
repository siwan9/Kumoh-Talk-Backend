package com.example.demo.domain.board.service.implement;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.board.service.entity.BoardTitleInfo;
import com.example.demo.domain.board.service.repository.LikeRepository;
import com.example.demo.global.base.dto.page.GlobalPageResponse;
import com.example.demo.global.base.exception.ErrorCode;
import com.example.demo.global.base.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikeHandler {
    private final LikeRepository likeRepository;

    @Transactional
    public void increaseLike(Long userId, Long boardId) {
        likeRepository.saveLike(userId,boardId);
    }


    @Transactional(readOnly = true)
    public GlobalPageResponse<BoardTitleInfo> getLikes(Long userId, Pageable pageable) {
        return GlobalPageResponse.create(likeRepository.findLikedBoardPageByUserId(userId, pageable));
    }


    @Transactional
    public void decreaseLike(Long userId, Long boardId) {
        likeRepository.deleteLike(userId,boardId);
    }

    public void validateExistLike(Long boardId, Long userId) {
        if (likeRepository.existsByBoardIdAndUserId(boardId, userId)) {
            throw new ServiceException(ErrorCode.USER_ALREADY_LIKE_BOARD);
        }
    }

    public void validateNonExistLike(Long boardId, Long userId) {
        if (!likeRepository.existsByBoardIdAndUserId(boardId, userId)) {
            throw new ServiceException(ErrorCode.USER_NOT_LIKE_BOARD);
        }
    }
}
