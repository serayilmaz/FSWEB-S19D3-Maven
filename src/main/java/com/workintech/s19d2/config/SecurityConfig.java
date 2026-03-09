package com.workintech.s19d2.config;

import com.workintech.s19d2.dao.MemberRepository;
import com.workintech.s19d2.entity.Member;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/workintech/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/workintech/accounts/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/workintech/accounts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/workintech/accounts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/workintech/accounts/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(MemberRepository memberRepository) {
        return username -> {
            Member member = memberRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            Set<GrantedAuthority> authorities = member.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                    .collect(Collectors.toSet());

            return User.withUsername(member.getEmail())
                    .password(member.getPassword())
                    .authorities(authorities)
                    .build();
        };
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> Set.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
