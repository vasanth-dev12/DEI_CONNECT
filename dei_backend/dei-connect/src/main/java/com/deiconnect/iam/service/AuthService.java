package com.deiconnect.iam.service;

import com.deiconnect.iam.dto.AuthResponse;
import com.deiconnect.iam.dto.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}
