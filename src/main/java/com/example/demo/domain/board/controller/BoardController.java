package com.example.demo.domain.board.controller;


import static com.example.demo.global.base.dto.ResponseUtil.*;

import com.example.demo.domain.board.domain.dto.request.BoardCreateRequest;
import com.example.demo.domain.board.domain.dto.request.BoardUpdateRequest;
import com.example.demo.domain.board.domain.dto.response.BoardInfoResponse;
import com.example.demo.domain.board.domain.dto.response.BoardPageResponse;
import com.example.demo.domain.board.domain.dto.response.BoardTitleInfoResponse;
import com.example.demo.domain.board.service.usecase.BoardUseCase;
import com.example.demo.global.aop.AssignUserId;
import com.example.demo.global.base.dto.ResponseBody;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardController {
    private final BoardUseCase boardUsecase;

    @AssignUserId
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN') and isAuthenticated()") //TODO : 세미나 신청을 한적 있어야 세미나 게시물 작성 가능하도록 변경 현재는 도메인 로직 안에서 처리
    @PostMapping("/v1/boards")
    public ResponseEntity<ResponseBody<BoardInfoResponse>> saveDraft(Long userId,
                                                  @RequestBody @Valid BoardCreateRequest boardCreateRequest)  {
            return ResponseEntity.ok(createSuccessResponse(boardUsecase.saveDraftBoard(userId, boardCreateRequest)));
    }

    @GetMapping("/v1/boards/{boardId}")
    public ResponseEntity<ResponseBody<BoardInfoResponse>> search(@PathVariable Long boardId) {
        return ResponseEntity.ok(createSuccessResponse(boardUsecase.searchSingleBoard(boardId)));
    }


    @AssignUserId
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN') and isAuthenticated()") //TODO : 게시물 수정 로직 변경 필요
    @PatchMapping("/v1/boards")
    public ResponseEntity<ResponseBody<BoardInfoResponse>> update(Long userId,
                                                        @RequestBody @Valid BoardUpdateRequest boardUpdateRequest)  {
        return ResponseEntity.ok(createSuccessResponse(boardUsecase.updateBoard(userId,boardUpdateRequest)));
    }

    @AssignUserId
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN') and isAuthenticated()")
    @DeleteMapping("/v1/boards/{boardId}")
    public ResponseEntity<ResponseBody<Void>> delete(Long userId,@PathVariable Long boardId) {
        boardUsecase.deleteBoard(userId,boardId);
        return ResponseEntity.ok(createSuccessResponse());
    }


    @GetMapping("/v1/boards")
    public ResponseEntity<ResponseBody<BoardPageResponse>> findBoardPageList(
        @PageableDefault(page=0, size=10,sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(createSuccessResponse(boardUsecase.findBoardList(pageable)));
    }

}
