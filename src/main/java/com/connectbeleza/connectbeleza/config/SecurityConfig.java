package com.connectbeleza.connectbeleza.config;

import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.repository.UsuarioRepository;
import com.connectbeleza.connectbeleza.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Público
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profissionais/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/forums/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/produtos/patrocinados").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                        // Profissional
                        .requestMatchers("/profissional/**")
                        .hasAnyRole("PROFISSIONAL", "ADMIN")
                        // Empresa
                        .requestMatchers("/empresa/**")
                        .hasAnyRole("EMPRESA", "ADMIN")
                        // Autenticado (cliente, profissional, empresa)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─── JWT FILTER ────────────────────────────────────────────────────────────

    @Component
    @RequiredArgsConstructor
    public static class JwtFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;
        private final UsuarioRepository usuarioRepository;

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain chain)
                throws ServletException, IOException {

            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);
            if (!jwtUtil.isTokenValido(token)) {
                chain.doFilter(request, response);
                return;
            }

            String email = jwtUtil.extrairEmail(token);
            usuarioRepository.findByEmail(email).ifPresent(usuario -> {
                UserDetails ud = buildUserDetails(usuario);
                var auth = new UsernamePasswordAuthenticationToken(
                        ud, null, ud.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            });

            chain.doFilter(request, response);
        }

        private UserDetails buildUserDetails(Usuario u) {
            // username = UUID do usuário para recuperação fácil nos controllers
            return User.withUsername(u.getId().toString())
                    .password(u.getSenha())
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())))
                    .build();
        }
    }
}