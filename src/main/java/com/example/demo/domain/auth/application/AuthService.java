package com.example.demo.domain.auth.application;

import com.example.demo.domain.auth.dto.request.JoinRequest;
import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.dto.response.LoginResponse;
import com.example.demo.domain.user.domain.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.auth.token.application.TokenProvider;
import com.example.demo.global.base.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.demo.global.base.exception.ErrorCode.EXIST_SAME_EMAIL;
import static com.example.demo.global.base.exception.ErrorCode.MISMATCH_EMAIL_OR_PASSWORD;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;

    @Transactional
    public void join(JoinRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = request.toEntity(encodedPassword);

        if (!isNotExistEmail(request.getEmail())) {
            throw new ServiceException(EXIST_SAME_EMAIL);
        }
        userRepository.save(newUser);
    }

    public boolean isNotExistEmail(String email) {
        return !userRepository.existsByEmail(email);
    }

    public LoginResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            String accessToken = tokenProvider.createAccessToken(authentication);

            return new LoginResponse(accessToken, "Bearer");
        } catch (Exception e) {
            // 이메일 & 비밀번호를 따로 예외처리를 하면 이메일을 유추할 수 있게되기에 공통 처리
            throw new ServiceException(MISMATCH_EMAIL_OR_PASSWORD);
        }

    }
}