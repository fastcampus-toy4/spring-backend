package com.toy4.jeommechu.controller;

import com.toy4.jeommechu.dto.AuthRequest;
import com.toy4.jeommechu.dto.AuthResponse;
import com.toy4.jeommechu.dto.RegisterRequest;
import com.toy4.jeommechu.model.User;
import com.toy4.jeommechu.repository.UserRepository;
import com.toy4.jeommechu.security.JwtUtil;
import com.toy4.jeommechu.service.AuthService;   // 추가
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final AuthService authService;      // 추가

    public AuthController(
            UserRepository userRepo,
            PasswordEncoder encoder,
            JwtUtil jwtUtil,
            AuthService authService             // 주입 순서에 추가
    ) {
        this.userRepo    = userRepo;
        this.encoder    = encoder;
        this.jwtUtil    = jwtUtil;
        this.authService = authService;       // 할당
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest req,
            HttpServletResponse resp
    ) {
        // 1) DB에서 유저 조회
        Optional<User> opt = userRepo.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = opt.get();
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2) JWT 생성
        String token = jwtUtil.generateToken(user.getEmail());

        // 3) HttpOnly 쿠키에 담기
        ResponseCookie cookie = ResponseCookie.from("JWT", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();
        resp.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // ─── 여기서 FastAPI 로 토큰 전달 ───
//        authService.forwardToken(token);
        // ────────────────────────────────

        // 4) 토큰 리턴
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        // (기존 코드 그대로)
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("이미 존재하는 이메일입니다.");
        }
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
