package com.codinglk.photoapp.api.user.security;

import com.codinglk.photoapp.api.user.service.UsersService;
import com.codinglk.photoapp.api.user.shared.UserDto;
import com.codinglk.photoapp.api.user.ui.model.LoginRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UsersService usersService;
    private Environment environment;

    public AuthenticationFilter(UsersService usersService, Environment environment, AuthenticationManager authenticationManager) {
        this.usersService = usersService;
        this.environment = environment;
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {

            LoginRequestModel creds = new ObjectMapper()
                    .readValue(req.getInputStream(), LoginRequestModel.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getEmail(),
                            creds.getPassword(),
                            new ArrayList<>())
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // We will not call this method, it will be called by Spring framework if authentication is successful
    // We will provide the implementation of this method
    // The job of this method will be to take user details and then use those user details (username and password) to generate the JWT token and add this
    // JWT token to HTTP Response header and return it back with HTTP response, the client application will be able to read this JWT token from HTTP response
    // header and use this JWT token in subsequent requests through our application as authorization header.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
       // super.successfulAuthentication(request, response, chain, authResult);
        String userName = ( (User) authResult.getPrincipal()).getUsername();
        UserDto userDetails = usersService.getUserDetailsByEmail(userName);

        String token = Jwts.builder()
                .setSubject(userDetails.getUserId())
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(environment.getProperty("token.expiration_time"))))
                .signWith(SignatureAlgorithm.HS512, environment.getProperty("token.secret") )
                .compact();

        response.setHeader("token", token);
        response.setHeader("userId", userDetails.getUserId());

    }
}
