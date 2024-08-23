package com.example.demo.domain.newsletter.controller;

import com.example.demo.domain.newsletter.domain.dto.request.NewsletterSubscribeRequest;
import com.example.demo.domain.newsletter.domain.dto.response.NewsletterInfo;
import com.example.demo.domain.newsletter.service.NewsletterService;
import com.example.demo.global.aop.AssignUserId;
import com.example.demo.global.base.dto.ResponseBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.base.dto.ResponseUtil.createSuccessResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/newsletters")
public class NewsletterController {

    private final NewsletterService newsletterService;

    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PostMapping("/subscribe")
    public ResponseEntity<ResponseBody<Void>> subscribe(Long userId,
                                                        @RequestBody @Valid NewsletterSubscribeRequest request) {
        newsletterService.subscribe(userId, request);
        return ResponseEntity.ok(createSuccessResponse());
    }

    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @GetMapping ("/subscribe")
    public ResponseEntity<ResponseBody<NewsletterInfo>> getNewsletterInfo(Long userId) {
        return ResponseEntity.ok(createSuccessResponse(newsletterService.getNewsletterInfo(userId)));
    }
}
