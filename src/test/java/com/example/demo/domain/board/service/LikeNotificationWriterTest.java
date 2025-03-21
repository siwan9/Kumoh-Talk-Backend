package com.example.demo.domain.board.service;

import com.example.demo.domain.board.service.entity.LikeInfo;
import com.example.demo.domain.comment.TransactionalTask;
import com.example.demo.domain.notification.entity.NotificationInfo;
import com.example.demo.domain.notification.entity.vo.NotificationType;
import com.example.demo.domain.notification.implement.LikeNotificationWriter;
import com.example.demo.infra.board.entity.Like;
import com.example.demo.infra.builder.JpaTestFixtureBuilder;
import com.example.demo.infra.user.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;

import static com.example.demo.fixture.user.UserFixtures.ADMIN_USER;
import static com.example.demo.fixture.user.UserFixtures.SEMINAR_WRITER_USER;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@ActiveProfiles("test")
@Import(value = {JpaTestFixtureBuilder.class, TransactionalTask.class})
public class LikeNotificationWriterTest {
    @Autowired
    private TransactionalTask transactionalTask;

    @Autowired
    private LikeNotificationWriter likeNotificationWriter;

    @Nested
    @DisplayName("<좋아요 생성 시 연관 알림 생성 통합 테스트>")
    class saveLike {
        private final User boardWriterUser = SEMINAR_WRITER_USER();
        private final User likeUser = ADMIN_USER();

        private Like savedLike;
        private LikeInfo savedlikeInfo;

        @BeforeEach
        void setUp() {
            savedLike = transactionalTask.createBoardAndLike(boardWriterUser, likeUser);
            savedlikeInfo = LikeInfo.builder()
                    .likeId(savedLike.getId())
                    .userId(likeUser.getId())
                    .userNickname(likeUser.getNickname())
                    .boardId(savedLike.getBoard().getId())
                    .build();
        }

        @AfterEach
        void tearDown() {
            transactionalTask.cleanUp();
        }

        @Test
        void 성공_좋아요_저장_시_알림도_저장된다() {
            // when
            CompletableFuture<NotificationInfo> completableFuture = likeNotificationWriter.saveLikeNotification(savedlikeInfo);

            // then
            NotificationInfo notificationInfo = completableFuture.join();

            assertSoftly(softly -> {
                softly.assertThat(notificationInfo.getInvokerId()).isEqualTo(savedLike.getId());
                softly.assertThat(notificationInfo.getInvokerType()).isEqualTo(NotificationType.BOARD_LIKE);
                softly.assertThat(notificationInfo.getBoardId()).isEqualTo(savedLike.getBoard().getId());
                softly.assertThat(notificationInfo.getSenderNickname()).isEqualTo(savedLike.getUser().getNickname());

//                List<NotificationUser> notificationUserList = notification.getNotificationUserList();
//                softly.assertThat(notificationUserList).isNotEmpty();
//                softly.assertThat(notificationUserList.get(0).getUser().getId()).isEqualTo(savedLike.getBoard().getUser().getId());
//                softly.assertThat(notificationUserList.size()).isEqualTo(1);
            });
        }
    }
}
