package com.servicoja.seguranca;

import com.servicoja.infra.excecao.ErroResposta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SegurancaConfig {

    private final JwtFiltro jwtFiltro;
    private final ObjectMapper objectMapper;

    public SegurancaConfig(JwtFiltro jwtFiltro, ObjectMapper objectMapper) {
        this.jwtFiltro = jwtFiltro;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(configuracaoCors()))
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(tratamento -> tratamento
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            objectMapper.writeValue(response.getWriter(),
                                    ErroResposta.de(401, "NAO_AUTENTICADO", "Autenticacao necessaria para acessar este recurso."));
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            objectMapper.writeValue(response.getWriter(),
                                    ErroResposta.de(403, "ACESSO_NEGADO", "Voce nao tem permissao para realizar esta acao."));
                        }))
                .authorizeHttpRequests(requisicoes -> requisicoes
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/cadastro/cliente", "/api/auth/cadastro/empresa",
                                "/api/auth/login", "/api/auth/refresh", "/api/auth/recuperar-senha",
                                "/api/auth/redefinir-senha").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        .requestMatchers("/api/asaas/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias/todas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/empresas/minhas").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/empresas", "/api/empresas/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/avaliacoes/empresas/*").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder codificadorSenhas() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource configuracaoCors() {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOriginPatterns(List.of("*"));
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));
        configuracao.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }
}
