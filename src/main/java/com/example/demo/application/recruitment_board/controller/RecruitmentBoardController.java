package com.example.demo.application.recruitment_board.controller;

import com.example.demo.application.recruitment_board.api.RecruitmentBoardApi;
import com.example.demo.application.recruitment_board.dto.request.RecruitmentBoardInfoAndFormRequest;
import com.example.demo.application.recruitment_board.dto.response.RecruitmentBoardInfoAndFormResponse;
import com.example.demo.application.recruitment_board.dto.response.RecruitmentBoardInfoResponse;
import com.example.demo.application.recruitment_board.dto.response.RecruitmentBoardSummaryResponse;
import com.example.demo.application.recruitment_board.dto.response.RecruitmentFormQuestionResponse;
import com.example.demo.domain.board.service.entity.vo.Status;
import com.example.demo.domain.recruitment_board.entity.RecruitmentBoardInfo;
import com.example.demo.domain.recruitment_board.entity.vo.RecruitmentBoardType;
import com.example.demo.domain.recruitment_board.service.RecruitmentBoardService;
import com.example.demo.global.aop.AssignUserId;
import com.example.demo.global.base.dto.ResponseBody;
import com.example.demo.global.base.dto.page.GlobalNoOffsetPageResponse;
import com.example.demo.global.base.dto.page.GlobalPageResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.demo.global.base.dto.ResponseUtil.createSuccessResponse;

@RestController
@RequestMapping("/api/v1/recruitment-boards")
@RequiredArgsConstructor
public class RecruitmentBoardController implements RecruitmentBoardApi {
    private final RecruitmentBoardService recruitmentBoardService;
    private final Validator validator;

    // TODO : 마감기한이 지한 게시물은 삭제 처리?

    /**
     * [모집 게시물 저장 및 임시저장] <br>
     * 모집 게시물 저장, 임시저장 기능
     *
     * @param status [published, draft]
     * @apiNote 1. RequestParam으로 주어지는 status 값에 따라 저장 혹은 임시저장 수행 <br>
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_ACTIVE_USER')")
    @PostMapping()
    public ResponseEntity<ResponseBody<RecruitmentBoardInfoAndFormResponse>> postRecruitmentBoardAndForm(
            Long userId,
            @RequestParam Status status,
            @RequestBody RecruitmentBoardInfoAndFormRequest recruitmentBoardInfoAndFormRequest) throws MethodArgumentNotValidException {
        validateRecruitmentBoardInfoAndFormRequest(status, recruitmentBoardInfoAndFormRequest);

        return ResponseEntity.ok(createSuccessResponse(
                RecruitmentBoardInfoAndFormResponse.from(recruitmentBoardService.postBoardAndForm(recruitmentBoardInfoAndFormRequest.toDomain(null, userId, status)))));
    }

    /**
     * [홈 화면에 출력될 모집 게시물 페이징 리스트 무한 스크롤 조회] <br>
     * No-Offset으로 구현한 홈 화면 Published 모집 게시물 리스트 조회 기능
     *
     * @param size                 한 페이지의 사이즈
     * @param lastBoardId          이전 페이지 마지막 게시물 Id(nullable)
     * @param recruitmentBoardType [study, project, mentoring]
     * @apiNote 1. 이전 페이지에서 출력한 가장 마지막 게시물의 Id를 lastBoardId에 실어 요청하면, 다음 게시물부터 페이징 사이즈에 맞게 응답 <br>
     * 2. 가장 처음 요청을 위해 lastBoardId은 nullable로 설정 <br>
     * -> lastBoardId가 널이라면 서비스 로직에서 맨 처음 게시물 Id를 조회하여 그 게시물부터 페이징 사이즈에 맞게 응답 <br>
     * 3. 정렬 기준 : 모집 마감일 오름차순
     */
    @GetMapping("/no-offset")
    public ResponseEntity<ResponseBody<GlobalNoOffsetPageResponse<RecruitmentBoardSummaryResponse>>> getRecruitmentBoardListByNoOffset(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long lastBoardId,
            @RequestParam RecruitmentBoardType recruitmentBoardType
    ) {
        // TODO : 차단 기능 추가
        List<RecruitmentBoardInfo> RecruitmentBoardInfoList = recruitmentBoardService.getPublishedBoardListByNoOffset(size, lastBoardId, recruitmentBoardType);

        return ResponseEntity.ok(createSuccessResponse(
                GlobalNoOffsetPageResponse.from(size, RecruitmentBoardInfoList.stream().map(RecruitmentBoardSummaryResponse::from).collect(Collectors.toList()))));
    }

    /**
     * [지정한 타입 게시판에 출력할 모집 게시물 페이징 리스트 조회] <br>
     * 페이지 번호로 구현한 모집 게시판에서 출력할 게시물 페이징 리스트 조회 기능
     *
     * @param pageable             페이지 번호(page), 페이지 사이즈(size), 페이지 정렬 조건 및 정렬 방향(sort) <br>
     *                             -> 정렬 조건은 createdAt, recruitmentDeadline 중 선택 <br>
     *                             -> 정렬 방향은 asc, desc 중 선택
     * @param recruitmentBoardType [study, project, mentoring]
     */
    @GetMapping("/page-num")
    public ResponseEntity<ResponseBody<GlobalPageResponse<RecruitmentBoardSummaryResponse>>> getRecruitmentBoardListByPageNum(
            @PageableDefault(page = 0, size = 10, sort = "recruitmentDeadline", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam RecruitmentBoardType recruitmentBoardType
    ) {
        Page<RecruitmentBoardInfo> recruitmentBoardInfoPage = recruitmentBoardService.getPublishedBoardListByPageNum(pageable, recruitmentBoardType);
        return ResponseEntity.ok(createSuccessResponse(
                GlobalPageResponse.create(recruitmentBoardInfoPage.map(RecruitmentBoardSummaryResponse::from))));
    }

    /**
     * [모집 게시물 정보 상세조회] <br>
     * 모집 게시물 리스트에서 게시물 클릭 시 상세조회 하는 기능
     *
     * @apiNote 1. 임시저장 게시물은 작성자만 조회할 수 있도록 구현
     */
    @AssignUserId(required = false)
    @GetMapping("/{recruitmentBoardId}/board")
    public ResponseEntity<ResponseBody<RecruitmentBoardInfoResponse>> getRecruitmentBoardInfo(Long userId, @PathVariable Long recruitmentBoardId) {
        return ResponseEntity.ok(createSuccessResponse(
                RecruitmentBoardInfoResponse.from(recruitmentBoardService.getBoardInfo(userId, recruitmentBoardId))));
    }

    /**
     * [모집 게시물 신청폼 상세조회] <br>
     * 모집 게시물 신청 페이지에서 보여질 신청 질문 리스트 조회 기능
     *
     * @apiNote 1. 임서저장 신청폼은 작성자만 조회할 수 있도록 구현 <br>
     * 2. 마감기한이 지난 경우 작성자가 아니면 폼 조회를 할 수 없도록 구현
     */
    @AssignUserId(required = false)
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_ACTIVE_USER')")
    @GetMapping("/{recruitmentBoardId}/form")
    public ResponseEntity<ResponseBody<List<RecruitmentFormQuestionResponse>>> getRecruitmentFormInfo(Long userId, @PathVariable Long recruitmentBoardId) {
        return ResponseEntity.ok(createSuccessResponse(
                recruitmentBoardService.getFormInfoList(userId, recruitmentBoardId)
                        .stream()
                        .map(RecruitmentFormQuestionResponse::from)
                        .sorted(Comparator.comparing(RecruitmentFormQuestionResponse::getNumber))
                        .collect(Collectors.toList())));
    }

    /**
     * [모집 게시물 정보 및 신청폼 수정] <br>
     * 모집 게시물 정보 및 질문 리스트 수정
     *
     * @param status [published, draft]
     * @apiNote 1. 이미 모집한 유저가 있다면 수정을 못하도록 설정 <br>
     * 2. 기존 질문 수 > 수정 질문 수 : 수정 질문 수 만큼의 질문은 수정하고, 나머지는 hard Delete <br>
     * 3. 기존 질문 수 < 수정 질문 수 : 기존 질문 수 만큼은 질문을 수정하고, 나머지는 insert <br>
     * 4. 기존 질문 수 == 수정 질문 수 : 모두 수정
     * 5. 위의 과정을 객관식 선택지에도 그대로 적용함
     */
    // 게시물 작성 전 임시저장 게시물을 불러온 후 저장하면 해당 API 요청
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_ACTIVE_USER')")
    @PatchMapping("/{recruitmentBoardId}")
    public ResponseEntity<ResponseBody<RecruitmentBoardInfoAndFormResponse>> patchRecruitmentBoardAndForm(
            Long userId,
            @PathVariable Long recruitmentBoardId,
            @RequestParam Status status,
            @RequestBody RecruitmentBoardInfoAndFormRequest recruitmentBoardInfoAndFormRequest) throws MethodArgumentNotValidException {
        validateRecruitmentBoardInfoAndFormRequest(status, recruitmentBoardInfoAndFormRequest);

        return ResponseEntity.ok(createSuccessResponse(
                RecruitmentBoardInfoAndFormResponse.from(recruitmentBoardService.patchBoardAndForm(recruitmentBoardInfoAndFormRequest.toDomain(recruitmentBoardId, userId, status)))));
    }

    /**
     * [모집 게시물 및 신청폼 삭제] <br>
     * 모집 게시물 및 신청폼, 신청자, 신청서, 댓글을 OrphanRemoval 설정을 통해 Soft Delete
     *
     * @apiNote 1. SoftDelete 설정을 사용할거면 통일하는게 좋다고 생각하여 해당 도메인 관련 모든 엔티티에 Soft Delete 적용 <br>
     * -> OrpahnRemoval을 통해 삭제가 될 때 Hard Delete, Soft Delete가 섞이면 곤란할 것이라고 생각했기 때문 <br>
     * 2. 관리자 기능과 달리 로그인한 유저의 id와 댓글 작성 유저의 id를 비교하는 절차를 거쳐야하므로, isAuthorized 매개변수를 false로 설정함
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_ACTIVE_USER')")
    @DeleteMapping("/{recruitmentBoardId}")
    public ResponseEntity<ResponseBody<Void>> deleteRecruitmentBoardAndForm(
            Long userId,
            @PathVariable Long recruitmentBoardId) {
        recruitmentBoardService.deleteBoardAndForm(userId, recruitmentBoardId, false);
        return ResponseEntity.ok(createSuccessResponse());
    }

    /**
     * [사용자의 최근 임시저장 게시물 조회] <br>
     * 게시물 생성 전 임시저장 게시물을 불러올 것인지 여부를 물어보게 됨 -> 불러온다면 해당 기능을 통해 가장 최근 임시저장 게시물을 조회
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_ACTIVE_USER')")
    @GetMapping("/draft/latest")
    public ResponseEntity<ResponseBody<RecruitmentBoardInfoAndFormResponse>> getDraftRecruitmentBoard(
            Long userId) {
        return ResponseEntity.ok(createSuccessResponse(
                RecruitmentBoardInfoAndFormResponse.from(recruitmentBoardService.getLatestDraftBoardAndForm(userId))));
    }

    /**
     * [사용자의 임시저장 게시물 페이징 리스트 무한 스크롤 조회] <br>
     * No-Offset으로 구현한 사용자 임시저장 게시물 페이징 리스트 조회 기능
     *
     * @param size        한 페이지의 사이즈
     * @param lastBoardId 이전 페이지 마지막 게시물 Id(nullable)
     * @apiNote 1. 현재 도메인의 /no-offset API 설명 참조
     * 2. 정렬 기준 : 생성 날짜 내림차순
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_ACTIVE_USER')")
    @GetMapping("/draft")
    public ResponseEntity<ResponseBody<GlobalNoOffsetPageResponse<RecruitmentBoardSummaryResponse>>> getDraftRecruitmentBoardList(
            Long userId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long lastBoardId) {
        List<RecruitmentBoardInfo> recruitmentBoardInfoList = recruitmentBoardService.getDraftBoardListByUserId(userId, size, lastBoardId);
        return ResponseEntity.ok(createSuccessResponse(
                GlobalNoOffsetPageResponse.from(size, recruitmentBoardInfoList.stream().map(RecruitmentBoardSummaryResponse::from).collect(Collectors.toList()))));
    }

    /**
     * [사용자 작성 게시물 페이징 리스트 조회] <br>
     * 페이지 번호로 구현한 사용자 작성 게시물 페이징 리스트 조회 기능
     *
     * @param pageable             페이지 번호(page), 페이지 사이즈(size), 페이지 정렬 조건 및 정렬 방향(sort) <br>
     *                             -> 정렬 조건은 createdAt <br>
     *                             -> 정렬 방향은 asc, desc 중 선택
     * @param recruitmentBoardType [study, project, mentoring]
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_ACTIVE_USER')")
    @GetMapping("/my-boards")
    public ResponseEntity<ResponseBody<GlobalPageResponse<RecruitmentBoardSummaryResponse>>> getPublishedUserRecruitmentBoardList(
            Long userId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam RecruitmentBoardType recruitmentBoardType) {
        Page<RecruitmentBoardInfo> recruitmentBoardInfoPage = recruitmentBoardService.getPublishedBoardListByUserId(userId, pageable, recruitmentBoardType);
        return ResponseEntity.ok(createSuccessResponse(
                GlobalPageResponse.create(recruitmentBoardInfoPage.map(RecruitmentBoardSummaryResponse::from))));
    }

    // Valid 검사 메서드
    // 임시저장은 valid 검사를 하지 않음
    private void validateRecruitmentBoardInfoAndFormRequest(Status status, RecruitmentBoardInfoAndFormRequest request) throws MethodArgumentNotValidException {
        switch (status) {
            case PUBLISHED -> {
                Set<ConstraintViolation<RecruitmentBoardInfoAndFormRequest>> violations = validator.validate(request);
                if (!violations.isEmpty()) {
                    BindingResult bindingResult = new BeanPropertyBindingResult(request, "recruitmentBoardInfoAndFormRequest");
                    for (ConstraintViolation<RecruitmentBoardInfoAndFormRequest> violation : violations) {
                        bindingResult.addError(new ObjectError("recruitmentBoardInfoAndFormRequest", violation.getMessage()));
                    }
                    throw new MethodArgumentNotValidException(null, bindingResult);
                }
            }
            case DRAFT -> {
                return;
            }
        }
    }
}
