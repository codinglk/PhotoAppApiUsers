package com.codinglk.photoapp.api.user.security;

import com.codinglk.photoapp.api.user.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private Environment environment;
    private UsersService usersService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public  WebSecurity(Environment environment, UsersService usersService, BCryptPasswordEncoder bCryptPasswordEncoder){

        this.environment = environment;
        this.usersService = usersService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable();

//        Permit request for all URLs contains /users/any-action
//        httpSecurity.authorizeRequests().antMatchers("/users/**").permitAll();

        // Provide access for gateway IP address
        httpSecurity.authorizeRequests()
                .antMatchers("/**").hasIpAddress(environment.getProperty("gateway.ip"))
                .antMatchers(HttpMethod.GET,"actuator/health").hasIpAddress(environment.getProperty("gateway.ip"))
                .antMatchers(HttpMethod.GET,"actuator/circuitbreakerevents").hasIpAddress(environment.getProperty("gateway.ip"))
                .and()
                .addFilter(getAuthenticationFilter());

        // We need to disable frameOptions() to view the H2 Console
        // In our code example, H2 console URL is http://localhost:8082/photo-app-api-users/h2-console
        httpSecurity.headers().frameOptions().disable();
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception{
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(usersService, environment, authenticationManager());
      //  authenticationFilter.setAuthenticationManager(authenticationManager());

        // Add Customized Login URL, /login is the default URL, we can also customize it and add our own URL, we have added the customized URL here /users/login
        authenticationFilter.setFilterProcessesUrl(environment.getProperty("login.url.path"));
        return authenticationFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // Provide our project UserService and PasswordEncoder to Spring
        // Our project UserService needs to extend org.springframework.security.core.userdetails.UserDetailsService
        auth.userDetailsService(usersService).passwordEncoder(bCryptPasswordEncoder);
        super.configure(auth);
    }
}
