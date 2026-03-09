package com.workintech.s19d2.config;

import com.workintech.s19d2.dao.RoleRepository;
import com.workintech.s19d2.entity.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            roleRepository.findByAuthority("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().authority("ROLE_USER").build()));

            roleRepository.findByAuthority("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().authority("ROLE_ADMIN").build()));
        };
    }
}
