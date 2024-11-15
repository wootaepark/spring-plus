package org.example.expert.config;

import java.io.IOException;

import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.security.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "로그인 및 JWT 생성")
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

	private final JwtUtil jwtUtil;


	public LoginFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
		setFilterProcessesUrl("/auth/signin");
	}



	// 토큰 생성 후 아이디 패스워드를 담아서 메니저에 전달
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
		HttpServletResponse response) throws
		AuthenticationException {
		log.info("로그인 시도");

		try {
			SigninRequest signinRequest = new ObjectMapper().readValue(request.getInputStream(), SigninRequest.class);

			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
				new UsernamePasswordAuthenticationToken(signinRequest.getEmail(), signinRequest.getPassword());

			return getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);
			// 여기서 토큰은 JWT 가 아니라 인증 요청을 보내기 위한 토큰이다.
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}

	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authResult) throws IOException, ServletException {
		log.info("로그인 성공 및 JWT 생성");
		Long userId = ((UserDetailsImpl)authResult.getPrincipal()).getUser().getId();
		String email = ((UserDetailsImpl)authResult.getPrincipal()).getUser().getEmail();
		String nickname = ((UserDetailsImpl)authResult.getPrincipal()).getUsername();
		UserRole role = ((UserDetailsImpl)authResult.getPrincipal()).getUser().getUserRole();

		String token = jwtUtil.createToken(userId, email, nickname, role);
		response.setHeader("Authorization", token);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException,
		ServletException {
		log.info("로그인 실패: {}", failed.getMessage());
		response.setStatus(401);
	}
}
