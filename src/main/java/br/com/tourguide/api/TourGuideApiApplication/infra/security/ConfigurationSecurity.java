package br.com.tourguide.api.TourGuideApiApplication.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // Habilita o @PreAuthorize nos controllers
public class ConfigurationSecurity {

    /** Define as regras de segurança HTTP da API:
     * <ul>
     *   <li>Desabilita o CSRF (API REST, sem formulários);</li>
     *   <li>Não cria sessão (STATELESS): cada requisição se autentica pelo token JWT, sem JSESSIONID;</li>
     *   <li>Registra o SecurityFilter antes do UsernamePasswordAuthenticationFilter, para validar o token
     *   JWT e autenticar o usuário antes de o Spring Security checar as regras de acesso;</li>
     *   <li>Libera sem autenticação apenas o login, o /error, o fluxo de esqueci minha senha e o Swagger;</li>
     *   <li>Exige autenticação nas demais rotas;</li>
     *   <li>Configura o logout em /api/auth/logout retornando 200;</li>
     *   <li>Faz requisições não autenticadas receberem 401 em vez de redirecionar para uma página de login.</li>
     * </ul>
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityFilter securityFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/error",
                                "/api/usuario/esqueci-minha-senha", "/api/usuario/recuperar-senha").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated())
//                .formLogin(form -> form // Forma para nao precisar criar controller. Hoje tem o AutenticacaoController
//                        .loginProcessingUrl("/api/auth/login")
//                        .successHandler((req, res, a) -> res.setStatus(200))
//                        .failureHandler((req, res, e) -> res.setStatus(401)))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((req, res, a) -> res.setStatus(200)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    /** Expõe o AuthenticationManager que o Spring monta automaticamente (usando o UserDetailsService
     e o PasswordEncoder registrados) para que possa ser injetado no AutenticacaoController e
     usado para autenticar manualmente o usuário no login.
    */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** Define o BCrypt como algoritmo de hash de senhas: o Spring o usa para comparar a senha
    informada no login com o hash salvo no banco (e deve ser usado também ao cadastrar senhas).
     Obs: Bean obrigatorio quando se configura mysql
    */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
