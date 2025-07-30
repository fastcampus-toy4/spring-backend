package com.toy4.jeommechu.controller;

import com.toy4.jeommechu.dto.AuthRequest;
import com.toy4.jeommechu.dto.AuthResponse;
import com.toy4.jeommechu.dto.RegisterRequest;
import com.toy4.jeommechu.model.User;
import com.toy4.jeommechu.repository.UserRepository;
import com.toy4.jeommechu.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthController(AuthService authService,
                          UserRepository userRepo,
                          PasswordEncoder encoder) {
        this.authService = authService;
        this.userRepo    = userRepo;
        this.encoder     = encoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest req,
            HttpServletResponse resp
    ) {
        // 1) AuthService에 위임: 인증 → JWT 생성 → FastAPI 연동 → 토큰 반환
        String token = authService.authenticateAndForward(req.getEmail(), req.getPassword());

        // 2) HttpOnly 쿠키에 JWT 담기
        ResponseCookie cookie = ResponseCookie.from("JWT", token)
                .httpOnly(true)
                .secure(false)         // 운영 시에는 true로 변경
                .path("/")
                .maxAge(24 * 60 * 60)  // 24시간
                .sameSite("Lax")
                .build();
        resp.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 3) 바디에도 토큰 리턴
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        // 기존 이메일 중복 체크
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("이미 존재하는 이메일입니다.");
        }

        // 회원가입 로직
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setName(req.getName());
        user.setAge(req.getAge());
        user.setGender(req.getGender());
        userRepo.save(user);

        return ResponseEntity.ok("회원가입 완료");
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
