package org.example.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.business.exception.InvalidCredentialsException;
import org.example.utils.JwtService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;


@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final HandlerExceptionResolver resolver;

    public JwtFilter(JwtService jwtService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtService = jwtService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if (jwtService.validateToken(token)) {
                    String username = jwtService.extractUsername(token);
                    request.setAttribute("username", username);
                    request.setAttribute("user_id", jwtService.extractUserId(token));
                } else {
                    throw new InvalidCredentialsException("Invalid or expired token!\n");
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // If any exception is thrown, delegate it to the HandlerExceptionResolver.
            // This will trigger our GlobalExceptionHandler and return a consistent JSON error.
            resolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/users/login") || path.equals("/users"); // public routes
    }
}
