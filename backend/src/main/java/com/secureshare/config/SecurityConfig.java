package com.secureshare.config;
import com.secureshare.security.JwtAuthenticationFilter;
import com.secureshare.service.CustomUserDetailsService;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*; import java.util.List;
@Configuration @EnableWebSecurity @EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter; private final CustomUserDetailsService uds;
    public SecurityConfig(JwtAuthenticationFilter jwtFilter,CustomUserDetailsService uds){this.jwtFilter=jwtFilter;this.uds=uds;}
    @Bean public SecurityFilterChain chain(HttpSecurity http) throws Exception {
        http.csrf(c->c.disable()).cors(c->c.configurationSource(cors()))
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a->a.requestMatchers("/api/auth/**").permitAll().requestMatchers("/h2-console/**").permitAll().requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated())
            .headers(h->h.frameOptions(f->f.sameOrigin()))
            .authenticationProvider(authProvider()).addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class);
        return http.build();}
    @Bean public CorsConfigurationSource cors(){CorsConfiguration c=new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:3000","http://localhost:5173"));
        c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("*"));c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
    @Bean public DaoAuthenticationProvider authProvider(){DaoAuthenticationProvider p=new DaoAuthenticationProvider();p.setUserDetailsService(uds);p.setPasswordEncoder(encoder());return p;}
    @Bean public AuthenticationManager authManager(AuthenticationConfiguration c) throws Exception{return c.getAuthenticationManager();}
    @Bean public PasswordEncoder encoder(){return new BCryptPasswordEncoder(12);}
}
