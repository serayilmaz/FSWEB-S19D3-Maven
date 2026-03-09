package com.workintech.s19d2.service;

import com.workintech.s19d2.dto.RegisterResponse;
import com.workintech.s19d2.dto.RegistrationMember;

public interface AuthService {
    RegisterResponse register(RegistrationMember registrationMember);
}
