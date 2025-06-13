package org.example.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.utils.JwtService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

//@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    ObjectMapper mapper = new ObjectMapper();

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtService.validateToken(token)) {
                String username = jwtService.extractUsername(token);
                request.setAttribute("username", username);
                request.setAttribute("user_id", jwtService.extractUserId(token));
            } else {
                Map<String, String> error = Map.of("error", "Invalid or expired token!\n");

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(mapper.writeValueAsString(error));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/users/login") || path.equals("/users"); // public routes
    }
}
