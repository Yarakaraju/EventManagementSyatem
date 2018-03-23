/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.technocomp.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *
 * @author Ravi Varma Yarakaraj
 */
//@Configuration
//@EnableWebSecurity
//@Order(2)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyAppUserDetailsService myAppUserDetailsService;
    @Autowired
    private CustomTokenBasedRememberMeService tokenBasedRememberMeService;
    @Autowired
    private RememberMeAuthenticationProvider rememberMeAuthenticationProvider;
    @Autowired
    private AppAuthenticationEntryPoint appAuthenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/registration").permitAll()
                .antMatchers("/admin/**").hasAnyRole("ADMIN","USER")
                .antMatchers("/user/**").hasAnyRole("ADMIN","USER").and()
                .rememberMe().rememberMeServices(tokenBasedRememberMeService)
                .and().httpBasic().realmName("EMS APP REALM")
                .authenticationEntryPoint(appAuthenticationEntryPoint);;
                
    }

   
    protected void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(myAppUserDetailsService)
                .passwordEncoder(bCryptPasswordEncoder());
        auth.authenticationProvider(rememberMeAuthenticationProvider);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
