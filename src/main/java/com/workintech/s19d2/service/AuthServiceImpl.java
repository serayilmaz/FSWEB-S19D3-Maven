package com.workintech.s19d2.service;

import com.workintech.s19d2.dao.MemberRepository;
import com.workintech.s19d2.dao.RoleRepository;
import com.workintech.s19d2.dto.RegisterResponse;
import com.workintech.s19d2.dto.RegistrationMember;
import com.workintech.s19d2.entity.Member;
import com.workintech.s19d2.entity.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(MemberRepository memberRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterResponse register(RegistrationMember registrationMember) {
        memberRepository.findByEmail(registrationMember.email())
                .ifPresent(m -> { throw new RuntimeException("Email already exists"); });

        String roleInput = registrationMember.role().toUpperCase();
        if (!roleInput.equals("USER") && !roleInput.equals("ADMIN")) {
            throw new RuntimeException("Role must be USER or ADMIN");
        }

        Role role = roleRepository.findByAuthority("ROLE_" + roleInput)
                .orElseThrow(() -> new RuntimeException("Role not found: ROLE_" + roleInput));

        Member member = Member.builder()
                .email(registrationMember.email())
                .password(passwordEncoder.encode(registrationMember.password()))
                .roles(Set.of(role))
                .build();

        Member saved = memberRepository.save(member);

        return new RegisterResponse(saved.getId(), saved.getEmail(), roleInput, "Member registered");
    }
}
