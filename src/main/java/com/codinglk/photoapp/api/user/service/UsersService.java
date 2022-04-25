package com.codinglk.photoapp.api.user.service;

import com.codinglk.photoapp.api.user.shared.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

// Extends org.springframework.security.core.userdetails.UserDetailsService
// It is required for the flow of Spring Security
public interface UsersService extends UserDetailsService {

    public UserDto createUser(UserDto userDetails);
    public UserDto getUserDetailsByEmail(String email);
    public UserDto getUserByUserId(String userId);
}
