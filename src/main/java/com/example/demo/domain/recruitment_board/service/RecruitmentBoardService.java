package com.example.demo.domain.recruitment_board.service;

import com.example.demo.domain.board.service.entity.vo.Status;
import com.example.demo.domain.newsletter.event.EmailNotificationEvent;
import com.example.demo.domain.newsletter.strategy.MentoringNoticeEmailDeliveryStrategy;
import com.example.demo.domain.newsletter.strategy.ProjectNoticeEmailDeliveryStrategy;
import com.example.demo.domain.newsletter.strategy.StudyNoticeEmailDeliveryStrategy;
import com.example.demo.domain.recruitment_application.repository.RecruitmentApplicantRepository;
import com.example.demo.domain.recruitment_board.entity.RecruitmentBoardAndFormInfo;
import com.example.demo.domain.recruitment_board.entity.RecruitmentBoardInfo;
import com.example.demo.domain.recruitment_board.entity.RecruitmentFormQuestionInfo;
import com.example.demo.domain.recruitment_board.entity.vo.EntireBoardType;
import com.example.demo.domain.recruitment_board.entity.vo.RecruitmentBoardType;
import com.example.demo.domain.recruitment_board.implement.board.RecruitmentBoardReader;
import com.example.demo.domain.recruitment_board.implement.board.RecruitmentBoardWriter;
import com.example.demo.domain.recruitment_board.implement.form.RecruitmentFormQuestionReader;
import com.example.demo.domain.user.implement.UserReader;
import com.example.demo.global.base.exception.ErrorCode;
import com.example.demo.global.base.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentBoardService {
    private final UserReader userReader;
    private final RecruitmentBoardReader recruitmentBoardReader;
    private final RecruitmentBoardWriter recruitmentBoardWriter;
    private final RecruitmentFormQuestionReader recruitmentFormQuestionReader;
    private final ApplicationEventPublisher eventPublisher;

    private final RecruitmentApplicantRepository recruitmentApplicantRepository;


    @Transactional
    public RecruitmentBoardAndFormInfo postBoardAndForm(
            RecruitmentBoardAndFormInfo recruitmentBoardAndFormInfo) {
        userReader.findUser(recruitmentBoardAndFormInfo.getBoard().getUserId())
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        RecruitmentBoardAndFormInfo savedBoard = recruitmentBoardWriter.post(recruitmentBoardAndFormInfo);

        if (savedBoard.getBoard().getStatus() == Status.PUBLISHED) {
            publishEventFactory(savedBoard.getBoard());
        }

        return savedBoard;
    }

    @Transactional(readOnly = true)
    public List<RecruitmentBoardInfo> getPublishedBoardListByNoOffset(int size, Long lastBoardId, RecruitmentBoardType recruitmentBoardType) {
        RecruitmentBoardInfo lastBoardInfo = null;
        if (lastBoardId != null) {
            lastBoardInfo = recruitmentBoardReader.getById(lastBoardId)
                    .orElseThrow(() -> new ServiceException(ErrorCode.BOARD_NOT_FOUND));
        }
        return recruitmentBoardReader.getPublishedPageByNoOffset(size, lastBoardInfo, recruitmentBoardType);
    }

    @Transactional(readOnly = true)
    public Page<RecruitmentBoardInfo> getPublishedBoardListByPageNum(Pageable pageable, RecruitmentBoardType recruitmentBoardType) {
        return recruitmentBoardReader.getPublishedPageByPageNum(null, pageable, recruitmentBoardType);
    }

    @Transactional(readOnly = true)
    public RecruitmentBoardInfo getBoardInfo(Long userId, Long recruitmentBoardId) {
        RecruitmentBoardInfo recruitmentBoardInfo = recruitmentBoardReader.getById(recruitmentBoardId)
                .orElseThrow(() -> new ServiceException(ErrorCode.BOARD_NOT_FOUND));

        validateAccessToBoard(userId, recruitmentBoardInfo);

        return recruitmentBoardInfo;
    }

    @Transactional(readOnly = true)
    public List<RecruitmentFormQuestionInfo> getFormInfoList(Long userId, Long recruitmentBoardId) {
        RecruitmentBoardInfo recruitmentBoardInfo = recruitmentBoardReader.getById(recruitmentBoardId)
                .orElseThrow(() -> new ServiceException(ErrorCode.BOARD_NOT_FOUND));

        validateAccessToBoard(userId, recruitmentBoardInfo);
        validateDeadLine(userId, recruitmentBoardInfo);

        return recruitmentFormQuestionReader.getByBoarIdWithAnswerList(recruitmentBoardId);
    }

    @Transactional
    public RecruitmentBoardAndFormInfo patchBoardAndForm(
            RecruitmentBoardAndFormInfo recruitmentBoardAndFormInfo) {
        RecruitmentBoardInfo recruitmentBoardInfo = recruitmentBoardReader.getByIdByWithQuestionList(recruitmentBoardAndFormInfo.getBoard().getBoardId())
                .orElseThrow(() -> new ServiceException(ErrorCode.BOARD_NOT_FOUND));

        validateWriter(recruitmentBoardAndFormInfo.getBoard().getUserId(), recruitmentBoardInfo);
        validatePatchable(recruitmentBoardInfo);

        // 게시물 업데이트
        RecruitmentBoardAndFormInfo savedBoard = recruitmentBoardWriter.patch(recruitmentBoardAndFormInfo);
        if (isStatusChangedToPublished(recruitmentBoardInfo, savedBoard.getBoard())) {
            publishEventFactory(savedBoard.getBoard());
        }
        return savedBoard;
    }

    @Transactional
    public void deleteBoardAndForm(
            Long userId,
            Long recruitmentBoardId,
            boolean isAuthorized) {
        RecruitmentBoardInfo recruitmentBoardInfo = recruitmentBoardReader.getById(recruitmentBoardId)
                .orElseThrow(() -> new ServiceException(ErrorCode.BOARD_NOT_FOUND));

        if (!isAuthorized) {
            validateWriter(userId, recruitmentBoardInfo);
        }

        recruitmentBoardWriter.delete(recruitmentBoardInfo);
    }

    @Transactional(readOnly = true)
    public RecruitmentBoardAndFormInfo getLatestDraftBoardAndForm(Long userId) {
        userReader.findUser(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
        RecruitmentBoardInfo recruitmentBoardInfo = recruitmentBoardReader.getLatestDraftIdByUserId(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.BOARD_NOT_FOUND));

        List<RecruitmentFormQuestionInfo> recruitmentFormQuestionInfo = recruitmentFormQuestionReader.getByBoarIdWithAnswerList(recruitmentBoardInfo.getBoardId());

        return RecruitmentBoardAndFormInfo.of(recruitmentBoardInfo, recruitmentFormQuestionInfo);
    }

    @Transactional(readOnly = true)
    public List<RecruitmentBoardInfo> getDraftBoardListByUserId(Long userId, int size, Long lastBoardId) {
        userReader.findUser(userId).orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        if (lastBoardId != null) {
            recruitmentBoardReader.getById(lastBoardId)
                    .orElseThrow(() -> new ServiceException(ErrorCode.BOARD_NOT_FOUND));
        }

        return recruitmentBoardReader.getDraftPageByUserIdByNoOffset(userId, size, lastBoardId);
    }

    @Transactional(readOnly = true)
    public Page<RecruitmentBoardInfo> getPublishedBoardListByUserId(Long userId, Pageable pageable, RecruitmentBoardType recruitmentBoardType) {
        userReader.findUser(userId).orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        return recruitmentBoardReader.getPublishedPageByPageNum(userId, pageable, recruitmentBoardType);
    }


    public boolean isPublished(RecruitmentBoardInfo recruitmentBoardInfo) {
        return recruitmentBoardInfo.getStatus() == Status.PUBLISHED;
    }

    public boolean isStatusChangedToPublished(RecruitmentBoardInfo originBoardInfo, RecruitmentBoardInfo newBoardInfo) {
        return originBoardInfo.getStatus() == Status.DRAFT && newBoardInfo.getStatus() == Status.PUBLISHED;
    }

    public void validatePatchable(RecruitmentBoardInfo recruitmentBoardInfo) {
        // 신청자가 존재하면 수정 불가
        if (recruitmentApplicantRepository.existsByRecruitmentBoard_Id(recruitmentBoardInfo.getBoardId())) {
            throw new ServiceException(ErrorCode.RECRUITMENT_APPLICANT_EXIST);
        }
    }

    public void validateWriter(Long userId, RecruitmentBoardInfo recruitmentBoardInfo) {
        if (userId == null || !userId.equals(recruitmentBoardInfo.getUserId())) {
            throw new ServiceException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void validateAccessToBoard(Long userId, RecruitmentBoardInfo recruitmentBoardInfo) {
        if (!isPublished(recruitmentBoardInfo)) {
            try {
                validateWriter(userId, recruitmentBoardInfo);
            } catch (ServiceException e) {
                throw new ServiceException(ErrorCode.DRAFT_NOT_ACCESS_USER);
            }
        }
    }

    public void validateDeadLine(Long userId, RecruitmentBoardInfo recruitmentBoardInfo) {
        if (recruitmentBoardInfo.getRecruitmentDeadline().isBefore(LocalDateTime.now())) {
            try {
                validateWriter(userId, recruitmentBoardInfo);
            } catch (ServiceException e) {
                throw new ServiceException(ErrorCode.DEADLINE_EXPIRED);
            }
        }
    }

    public void publishEventFactory(RecruitmentBoardInfo recruitmentBoardInfo) {
        if (recruitmentBoardInfo.getType() == RecruitmentBoardType.STUDY) {
            eventPublisher.publishEvent(
                    EmailNotificationEvent.create(
                            EntireBoardType.fromRecruitmentBoardType(recruitmentBoardInfo.getType()),
                            StudyNoticeEmailDeliveryStrategy.create(recruitmentBoardInfo))
            );
        } else if (recruitmentBoardInfo.getType() == RecruitmentBoardType.PROJECT) {
            eventPublisher.publishEvent(
                    EmailNotificationEvent.create(
                            EntireBoardType.fromRecruitmentBoardType(recruitmentBoardInfo.getType()),
                            ProjectNoticeEmailDeliveryStrategy.create(recruitmentBoardInfo))
            );
        } else if (recruitmentBoardInfo.getType() == RecruitmentBoardType.MENTORING) {
            eventPublisher.publishEvent(
                    EmailNotificationEvent.create(
                            EntireBoardType.fromRecruitmentBoardType(recruitmentBoardInfo.getType()),
                            MentoringNoticeEmailDeliveryStrategy.create(recruitmentBoardInfo)
                    )
            );
        }
    }
}
