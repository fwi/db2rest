package com.github.fwi.db2restapp;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

@Configuration
@Import(SecurityAutoConfiguration.class)
@EnableWebSecurity
public class AppBasicAuth extends WebSecurityConfigurerAdapter {

	@Value("${db2rest.auth.basic.username:db2rest}")
	private String username;

	@Value("${db2rest.auth.basic.password:db2rest}")
	private String password;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		var pencoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		auth.inMemoryAuthentication()
			.withUser(username)
			.password(pencoder.encode(password))
			.authorities(Collections.emptyList());
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http.antMatcher("/**")
			.csrf().disable().httpBasic()
			.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and().authorizeRequests().anyRequest().fullyAuthenticated();
	}
	
}
