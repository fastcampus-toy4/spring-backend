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

    public AuthService(
            UserRepository userRepo,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            RestTemplate restTemplate,
            @Value("${fastapi.url}") String fastapiBaseUrl
    ) {
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
     * Spring에서 발급한 JWT를 FastAPI에 전달합니다.
     */
    public void forwardToken(String token) {
        String url = fastapiBaseUrl + "/api/auth/login";
        System.out.println("[DEBUG] Calling FastAPI at: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

        System.out.println("[DEBUG] FastAPI status: " + resp.getStatusCode());
        System.out.println("[DEBUG] FastAPI body:   " + resp.getBody());

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("FastAPI 연동 실패: " + resp.getStatusCode());
        }
    }
}
