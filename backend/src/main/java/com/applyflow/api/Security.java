package com.applyflow.api;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration @EnableMethodSecurity
class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
  @Bean SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwt) throws Exception {
    return http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a->a.requestMatchers("/api/v1/auth/**","/actuator/health","/swagger-ui/**","/v3/api-docs/**").permitAll().anyRequest().authenticated())
      .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
  }
  @Bean org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String origins){
    var c=new org.springframework.web.cors.CorsConfiguration(); c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s->!s.isBlank()).toList()); c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS")); c.setAllowedHeaders(List.of("*"));
    var s=new org.springframework.web.cors.UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/**",c); return s;
  }
}

@Component
class JwtService {
  private final javax.crypto.SecretKey key; private final long expiry;
  JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-minutes}") long minutes){key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));expiry=minutes*60_000;}
  String create(UUID userId,String email){return Jwts.builder().subject(userId.toString()).claim("email",email).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+expiry)).signWith(key).compact();}
  UUID parse(String token){return UUID.fromString(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());}
}

@Component
class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwt; JwtFilter(JwtService jwt){this.jwt=jwt;}
  protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,java.io.IOException {
    String h=request.getHeader("Authorization");
    if(h!=null && h.startsWith("Bearer ")){try{UUID id=jwt.parse(h.substring(7)); var auth=new UsernamePasswordAuthenticationToken(id,null,List.of(new SimpleGrantedAuthority("ROLE_USER"))); SecurityContextHolder.getContext().setAuthentication(auth);}catch(JwtException|IllegalArgumentException ignored){response.setStatus(401);return;}}
    chain.doFilter(request,response);
  }
}

final class CurrentUser { static UUID id(){Object p=SecurityContextHolder.getContext().getAuthentication().getPrincipal(); if(p instanceof UUID u)return u; throw new AuthenticationCredentialsNotFoundException("Authentication required");} private CurrentUser(){} }
