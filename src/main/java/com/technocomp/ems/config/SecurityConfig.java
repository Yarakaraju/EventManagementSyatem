package com.technocomp.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
//@Order(3)
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private String tokenKey = "EMS_TOKEN";

    @Autowired
    private MyAppUserDetailsService myAppUserDetailsService;
    @Autowired
    private AppAuthenticationEntryPoint appAuthenticationEntryPoint;

    @Autowired
    private CustomTokenBasedRememberMeService tokenBasedRememberMeService;

    @Autowired
    private RememberMeAuthenticationProvider rememberMeAuthenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println("Inside configure in securityconfig");
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/verifyOTP").permitAll()
                .antMatchers("/sendOTP").permitAll()
                .antMatchers("/registration").permitAll()
                .antMatchers("/api/**").hasAnyAuthority("ADMIN","USER")
                .and()
                 .httpBasic().realmName("EMS_REALM")
                .authenticationEntryPoint(appAuthenticationEntryPoint).and()
                .rememberMe().rememberMeServices(tokenBasedRememberMeService).and()
                .addFilterBefore(rememberMeAuthenticationFilter(), BasicAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint());

        /*.antMatchers("/admin/**").hasAnyRole("ADMIN", "USER")
                .antMatchers("/api/**").hasAnyRole("ADMIN", "USER")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER").and()
               */
        
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        auth.userDetailsService(myAppUserDetailsService).passwordEncoder(passwordEncoder);
    }

    protected void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(myAppUserDetailsService)
                .passwordEncoder(bCryptPasswordEncoder());
        auth.authenticationProvider(rememberMeAuthenticationProvider);
    }

    @Bean
    public RememberMeAuthenticationFilter rememberMeAuthenticationFilter() throws Exception {
        return new RememberMeAuthenticationFilter(authenticationManager(), tokenBasedRememberMeService());
    }

    @Bean
    public CustomTokenBasedRememberMeService tokenBasedRememberMeService() {
        CustomTokenBasedRememberMeService service = new CustomTokenBasedRememberMeService(tokenKey, myAppUserDetailsService);
        service.setAlwaysRemember(true);
        service.setCookieName("at");
        return service;
    }

    @Bean
    RememberMeAuthenticationProvider rememberMeAuthenticationProvider() {
        return new RememberMeAuthenticationProvider(tokenKey);
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
