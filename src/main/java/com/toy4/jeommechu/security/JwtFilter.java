package com.toy4.jeommechu.security;

import com.toy4.jeommechu.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public JwtFilter(JwtUtil jwtUtil,
                     @Lazy AuthService authService) {
        this.jwtUtil     = jwtUtil;
        this.authService = authService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path   = request.getServletPath();
        String method = request.getMethod();

        // 1) /api/auth/ 로 시작하는 모든 경로는 스킵 (login, register 등)
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        // 2) 모든 OPTIONS 프리플라이트 스킵
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 쿠키에서 JWT 토큰 찾기
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 토큰이 유효하면 SecurityContext에 인증 설정
        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.getSubject(token);
            var userDetails = authService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            auth.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 필터 체인 계속
        filterChain.doFilter(request, response);
    }
}
