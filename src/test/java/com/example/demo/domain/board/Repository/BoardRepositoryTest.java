package com.example.demo.domain.board.Repository;

import com.example.demo.base.RepositoryTest;
import com.example.demo.domain.board.domain.BoardStatus;
import com.example.demo.domain.board.domain.entity.Board;
import com.example.demo.domain.category.repository.CategoryRepository;
import com.example.demo.domain.file.domain.FileType;
import com.example.demo.domain.file.domain.entity.File;
import com.example.demo.domain.user.domain.User;
import com.example.demo.domain.user.domain.vo.Role;
import com.example.demo.domain.user.domain.vo.Status;
import com.example.demo.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("20200013@kumoh.ac.kr")
                .name("name")
                .nickname("nickname")
                .password("password")
                .role(Role.USER)
                .department("department")
                .status(Status.ATTENDING)
                .field("field")
                .build();
        userRepository.save(user);

    }

    @AfterEach
    public void clear() {
        user = null;
    }

    @Test
    @DisplayName("단독 Board 저장")
    void 저장() {
        //given
        Board board = Board.builder()
                .title("제목")
                .content("내용")
                .view(0L)
                .status(BoardStatus.FAKE)
                .user(user)
                .build();
        //when
        Board save = boardRepository.save(board);

        // then
        Board board1 = boardRepository.findById(save.getId()).get();
        assertThat(board1.getTitle()).isEqualTo(board.getTitle());
        assertThat(board1.getContent()).isEqualTo(board.getContent());
        assertThat(board1.getView()).isEqualTo(board.getView());
        assertThat(board1.getStatus()).isEqualTo(board.getStatus());
    }


}