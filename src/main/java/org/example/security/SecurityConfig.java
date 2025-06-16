package org.example.security;
import org.example.utils.JwtService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

/**
 * Security configuration class that sets up CORS and JWT filters.
 * CORS is configured to allow requests from a specific origin.
 * JWT filter is registered to handle authentication and authorization.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures CORS.
     *
     * @return FilterRegistrationBean for CORS filter
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // run this first
        return bean;
    }

    /**
     * Configures the JWT filter.
     *
     * @param jwtService The JWT service used for authentication
     * @param resolver The exception resolver to handle errors
     * @return JwtFilter instance
     */
    @Bean
    public JwtFilter jwtFilter(JwtService jwtService,
                               @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        return new JwtFilter(jwtService, resolver);
    }

    /**
     * Registers the JWT filter to intercept all requests.
     *
     * @param jwtFilter The JWT filter to be registered
     * @return FilterRegistrationBean for the JWT filter
     */
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE); // run this after CORS
        return registrationBean;
    }
}
