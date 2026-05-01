package docs.koda.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityBeans {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatchers(m -> m.requestMatchers(SwaggerRoutes.PATHS))
                .authorizeHttpRequests(r -> r.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }


    private static final class PublicRoutes {
        private static final String[] PATHS = {
                "/error",
                "/favicon.ico",
                "/api/v1/auth/login",
                "/oauth2/**",
                "/login/oauth2/**",
                "/api/v1/stufen",
                "/api/v1/stufen/**",
                "/api/v1/uebungen/**",
                "/api/v1/lager",
                "/api/v1/lager/upcoming",
                "/api/v1/lager/{slug}",
                "/api/v1/media/*/download",
        };
    }

    private static final class SwaggerRoutes {
        private static final String[] PATHS = {
                "/swagger-ui",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/v3/api-docs.yaml",
                "/webjars/**",
        };
    }
}
