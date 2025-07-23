package io.github.pavelshe11.authmicro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable()) // <--- ОТКЛЮЧАЕТ CSRF
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/auth/v1/registration").permitAll()
                                .requestMatchers("/auth/v1/registration/confirmEmail").permitAll()
                                .requestMatchers("/auth/v1/login/sendCodeEmail").permitAll()
                                .requestMatchers("/auth/v1/login/confirmEmail").permitAll()
                                .requestMatchers("/actuator/**").hasRole("admin")
                                .requestMatchers("/auth/v1/refreshToken").hasRole("user")

                                // for test
                                .requestMatchers("/auth/test/public").permitAll()
                                .requestMatchers("/auth/test/**").authenticated()
                                //

                                .anyRequest().authenticated()
                ).sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
//                .httpBasic(Customizer.withDefaults())
                .oauth2ResourceServer(
                        resourceServer -> resourceServer.jwt(
                                jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(
                                        keycloackAuthConverter()

                                )
                        )
                )
                .build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> keycloackAuthConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                new AuthoritiseConverter()
        );
        return converter;
    }
}
