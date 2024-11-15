package org.example.expert.config;

import java.beans.BeanProperty;
import java.util.HashMap;
import java.util.Map;

import org.example.expert.security.UserDetailsServiceImpl;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j(topic = "Security config")
public class WebSecurityConfig {


	private final AuthenticationConfiguration authenticationConfiguration;
	private final UserDetailsServiceImpl userDetailsService;
	private final JwtUtil jwtUtil;



	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}




	@Bean
	public LoginFilter loginFilter() throws Exception {
		LoginFilter filter =new LoginFilter(jwtUtil);
		filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
		return filter;

	}

	@Bean
	public JwtAuthorizationFilter jwtAuthorizationFilter() {
		return new JwtAuthorizationFilter(jwtUtil, userDetailsService);
	}



	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// CSRF 설정

		log.info("securityConfig 실행됨");
		http.csrf((csrf) -> csrf.disable())
			.formLogin((auth) -> auth.disable())
			.httpBasic((httpBasic) -> httpBasic.disable());

		http.sessionManagement((sessionManagement) ->
			sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		);

		http.authorizeHttpRequests((authorizeHttpRequests) ->
			authorizeHttpRequests
				.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // resources 접근 허용 설정
				.requestMatchers("/auth/**").permitAll() // '/auth' 로 시작하는 요청 모두 접근 허가
				.anyRequest().authenticated() // 그 외 모든 요청 인증처리
		);

		// form 로그인을 미사용하기 때문에
		// 해당 필터를 커스텀 로그인 필터로 변경

		http.addFilterBefore(jwtAuthorizationFilter(), LoginFilter.class); // JWT 인가 필터
		http.addFilterBefore(loginFilter(), UsernamePasswordAuthenticationFilter.class); // 로그인 필터





		return http.build();
	}

}
