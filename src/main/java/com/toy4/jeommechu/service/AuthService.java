package com.toy4.jeommechu.service;

import com.toy4.jeommechu.model.User;
import com.toy4.jeommechu.repository.UserRepository;
import com.toy4.jeommechu.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final String fastapiBaseUrl;

    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RestTemplate restTemplate,
                       @Value("${fastapi.url}") String fastapiBaseUrl) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.fastapiBaseUrl = fastapiBaseUrl;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("등록된 사용자가 없습니다: " + email));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    /**
     * 1) 인증 검증 → 2) JWT 생성 → 3) FastAPI로 토큰 전송 → 4) 클라이언트에 JWT 반환
     */
    public String authenticateAndForward(String email, String rawPassword) {
        // 1) 인증
        UserDetails ud = loadUserByUsername(email);
        if (!passwordEncoder.matches(rawPassword, ud.getPassword())) {
            throw new BadCredentialsException("비밀번호 불일치");
        }

        // 2) JWT 생성
        String jwt = jwtUtil.generateToken(ud.getUsername());

        // 3) FastAPI 호출 (Bearer 헤더)
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                fastapiBaseUrl + "/api/auth/login",
                HttpMethod.POST,
                request,
                String.class
        );
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("FastAPI 연동 실패: " + resp.getStatusCode());
        }

        // 4) 최종적으로 Spring 클라이언트에 JWT 반환
        return jwt;
    }
}
