package com.workintech.s19d2;

import com.workintech.s19d2.dao.AccountRepository;
import com.workintech.s19d2.dao.MemberRepository;
import com.workintech.s19d2.dao.RoleRepository;
import com.workintech.s19d2.dto.RegisterResponse;
import com.workintech.s19d2.dto.RegistrationMember;
import com.workintech.s19d2.entity.Account;
import com.workintech.s19d2.entity.Member;
import com.workintech.s19d2.entity.Role;
import com.workintech.s19d2.service.AccountServiceImpl;
import com.workintech.s19d2.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AccountServiceImpl accountService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(accountRepository);
        authService = new AuthServiceImpl(memberRepository, roleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Repository interfaces should extend JpaRepository")
    void repositoriesShouldExtendJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(AccountRepository.class));
        assertTrue(JpaRepository.class.isAssignableFrom(MemberRepository.class));
        assertTrue(JpaRepository.class.isAssignableFrom(RoleRepository.class));
    }

    @Test
    @DisplayName("Account getters/setters")
    void accountGetterSetterTest() {
        Account account = new Account();
        account.setId(1L);
        account.setName("Main Account");

        assertEquals(1L, account.getId());
        assertEquals("Main Account", account.getName());
    }

    @Test
    @DisplayName("Role getters/setters")
    void roleGetterSetterTest() {
        Role role = new Role();
        role.setId(10L);
        role.setAuthority("ROLE_USER");

        assertEquals(10L, role.getId());
        assertEquals("ROLE_USER", role.getAuthority());
    }

    @Test
    @DisplayName("Member getters/setters")
    void memberGetterSetterTest() {
        Role role = Role.builder().id(1L).authority("ROLE_USER").build();
        Member member = new Member();
        member.setId(5L);
        member.setEmail("user@test.com");
        member.setPassword("12345");
        member.setRoles(Set.of(role));

        assertEquals(5L, member.getId());
        assertEquals("user@test.com", member.getEmail());
        assertEquals("12345", member.getPassword());
        assertEquals(1, member.getRoles().size());
    }

    @Test
    @DisplayName("RegistrationMember record data")
    void registrationMemberRecordTest() {
        RegistrationMember dto = new RegistrationMember("user@test.com", "12345", "USER");
        assertEquals("user@test.com", dto.email());
        assertEquals("12345", dto.password());
        assertEquals("USER", dto.role());
    }

    @Test
    @DisplayName("RegisterResponse record data")
    void registerResponseRecordTest() {
        RegisterResponse response = new RegisterResponse(1L, "user@test.com", "USER", "Member registered");
        assertEquals(1L, response.id());
        assertEquals("user@test.com", response.email());
        assertEquals("USER", response.role());
        assertEquals("Member registered", response.message());
    }

    @Test
    @DisplayName("Find all accounts")
    void findAllAccountsTest() {
        Account acc = Account.builder().id(1L).name("A").build();
        when(accountRepository.findAll()).thenReturn(List.of(acc));

        List<Account> result = accountService.findAll();

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getName());
    }

    @Test
    @DisplayName("Save account")
    void saveAccountTest() {
        Account acc = Account.builder().name("New").build();
        Account saved = Account.builder().id(2L).name("New").build();

        when(accountRepository.save(acc)).thenReturn(saved);

        Account result = accountService.save(acc);

        assertEquals(2L, result.getId());
        verify(accountRepository).save(acc);
    }

    @Test
    @DisplayName("Register member successfully")
    void registerMemberSuccessTest() {
        RegistrationMember request = new RegistrationMember("admin@test.com", "12345", "ADMIN");
        Role adminRole = Role.builder().id(2L).authority("ROLE_ADMIN").build();

        when(memberRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("12345")).thenReturn("ENC_PASS");
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> {
            Member m = inv.getArgument(0);
            m.setId(99L);
            return m;
        });

        RegisterResponse response = authService.register(request);

        assertEquals(99L, response.id());
        assertEquals("admin@test.com", response.email());
        assertEquals("ADMIN", response.role());
        verify(roleRepository).findByAuthority(eq("ROLE_ADMIN"));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("Register should fail when email already exists")
    void registerMemberEmailExistsTest() {
        RegistrationMember request = new RegistrationMember("user@test.com", "12345", "USER");
        Member existing = Member.builder().id(1L).email("user@test.com").password("x").build();

        when(memberRepository.findByEmail("user@test.com")).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }
}
