package com.toy4.jeommechu.controller;

import com.toy4.jeommechu.dto.*;
import com.toy4.jeommechu.model.User;
import com.toy4.jeommechu.repository.UserRepository;
import com.toy4.jeommechu.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            UserRepository userRepo,
            PasswordEncoder encoder,
            JwtUtil jwtUtil
    ) {
        this.userRepo = userRepo;
        this.encoder  = encoder;
        this.jwtUtil  = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest req,
            HttpServletResponse resp
    ) {
        // 1) DB에서 유저 조회

        System.out.println("[DEBUG] req email=" + req.getEmail() + ", raw pw=" + req.getPassword());

        Optional<User> opt = userRepo.findByEmail(req.getEmail());
        System.out.println("[DEBUG] user found? " + opt.isPresent());

        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = opt.get();
        System.out.println("[DEBUG] stored pw hash=" + user.getPassword());
        System.out.println("[DEBUG] matches? " + encoder.matches(req.getPassword(), user.getPassword()));

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 3) JWT 생성
        String token = jwtUtil.generateToken(user.getEmail());

        // 4) HttpOnly 쿠키에 담기
        ResponseCookie cookie = ResponseCookie.from("JWT", token)
                .httpOnly(true)
                .secure(false)         // 운영 시 true로 변경
                .path("/")
                .maxAge(24 * 60 * 60)  // 24시간
                .sameSite("Lax")
                .build();
        resp.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 5) 토큰 리턴
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 이메일입니다.");
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
