package com.example.demo.infra.board.querydsl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.domain.board.service.entity.vo.BoardType;
import com.example.demo.domain.board.service.entity.BoardTitleInfo;
import com.example.demo.domain.board.service.entity.DraftBoardTitle;
import com.example.demo.infra.board.entity.Board;

public interface BoardDslRepository {
	Optional<Board> findBoardAndUserAndCategory(Long boardId);

	void increaseViewCount(Long boardId, Integer viewCount);

	Page<BoardTitleInfo> findBoardByPage(BoardType boardType, Pageable pageable);

	Page<DraftBoardTitle> findDraftBoardByPage(Long userId, Pageable pageable);

	Page<BoardTitleInfo> findPublishedBoardListByUser(Long userId, BoardType boardType, Pageable pageable);

	String getAttachFileUrl(Long boardId);
}
