package com.example.demo.infra.board.repository;

import com.example.demo.infra.board.entity.Board;
import com.example.demo.infra.board.querydsl.BoardDslRepository;
import com.example.demo.infra.recruitment_board.entity.CommentBoard;
import com.example.demo.infra.recruitment_board.repository.jpa.CommentBoardJpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface BoardJpaRepository extends JpaRepository<Board, Long>, CommentBoardJpaRepository, BoardDslRepository {
    @Query("SELECT COUNT(l) FROM Like l WHERE l.board.id = :boardId")
    long countLikesByBoardId(@Param("boardId") Long boardId);

    @Transactional
    @Modifying
    @Query("UPDATE Board b SET b.viewCount = b.viewCount + 1 WHERE b.id = :boardId")
    void increaseViewCount(@Param("boardId") Long boardId);

    @Override
    @Transactional(readOnly = true)
    default Optional<CommentBoard> doFindById(Long id) {
        Optional<Board> board = findById(id);
        if (board.isPresent()) {
            return Optional.of(board.get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Query("SELECT b FROM Board b " +
            "JOIN FETCH b.user " +
            "WHERE b.id = :id")
    Optional<CommentBoard> findByIdWithUser(Long id);
}

