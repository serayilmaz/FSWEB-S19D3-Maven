package com.workintech.s19d2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workintech.s19d2.config.SecurityConfig;
import com.workintech.s19d2.controller.AccountController;
import com.workintech.s19d2.controller.AuthController;
import com.workintech.s19d2.dto.RegisterResponse;
import com.workintech.s19d2.dto.RegistrationMember;
import com.workintech.s19d2.entity.Account;
import com.workintech.s19d2.service.AccountService;
import com.workintech.s19d2.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, AccountController.class})
@Import(SecurityConfig.class)
class ControllerTest {

    @MockBean
    private AccountService accountService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setName("Sample Account");
    }

    @Test
    @DisplayName("Find All Accounts")
    @WithMockUser(roles = {"ADMIN"})
    void findAll() throws Exception {
        given(accountService.findAll()).willReturn(List.of(account));

        mockMvc.perform(get("/workintech/accounts/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(account.getName())));
    }

    @Test
    @DisplayName("Save Account")
    @WithMockUser(roles = {"ADMIN"})
    void saveAccount() throws Exception {
        given(accountService.save(any(Account.class))).willReturn(account);

        mockMvc.perform(post("/workintech/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(account.getName())));
    }

    @Test
    @DisplayName("Register endpoint creates a new member")
    void registerCreatesNewMember() throws Exception {
        RegistrationMember request = new RegistrationMember("test@example.com", "password123", "USER");
        RegisterResponse response = new RegisterResponse(1L, "test@example.com", "USER", "Member registered");

        given(authService.register(any(RegistrationMember.class))).willReturn(response);

        mockMvc.perform(post("/workintech/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.message", is("Member registered")));
    }

    @Test
    void accessSecuredEndpointsWithoutAuthenticationShouldFail() throws Exception {
        mockMvc.perform(get("/workintech/accounts/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void accessGetWithUserRoleShouldSucceed() throws Exception {
        given(accountService.findAll()).willReturn(List.of(account));

        mockMvc.perform(get("/workintech/accounts/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void accessPostWithUserRoleShouldFail() throws Exception {
        mockMvc.perform(post("/workintech/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isForbidden());
    }
}
