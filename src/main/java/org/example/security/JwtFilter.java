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

/**
 * JwtFilter is a Spring filter that intercepts HTTP requests to validate JWT tokens.
 * It extracts the username and user ID from the token and sets them as request attributes.
 * If the token is invalid or expired, it throws an InvalidCredentialsException,
 * which is handled by a global exception handler.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final HandlerExceptionResolver resolver;

    /**
     * Constructs a JwtFilter with the specified JwtService and HandlerExceptionResolver.
     *
     * @param jwtService The service used for JWT operations.
     * @param resolver The exception resolver to handle exceptions thrown during filtering.
     */
    public JwtFilter(JwtService jwtService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtService = jwtService;
        this.resolver = resolver;
    }

    /**
     * Filters incoming HTTP requests to validate JWT tokens.
     * If the token is valid, it extracts the username and user ID and sets them as request attributes.
     * If the token is invalid or expired, it throws an InvalidCredentialsException.
     *
     * @param request The HTTP request to filter.
     * @param response The HTTP response.
     * @param filterChain The filter chain to continue processing the request.
     */
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

    /**
     * Determines whether the filter should not be applied to the request.
     * This filter does not apply to public routes like login and user registration.
     *
     * @param request The HTTP request to check.
     * @return true if the filter should not be applied, false otherwise.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/users/login") || path.equals("/users/create") || path.equals("/users"); // public routes
    }
}
