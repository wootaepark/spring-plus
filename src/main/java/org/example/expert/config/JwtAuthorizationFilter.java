package org.example.expert.config;

import java.io.IOException;

import org.example.expert.security.UserDetailsImpl;
import org.example.expert.security.UserDetailsServiceImpl;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String tokenValue = request.getHeader("Authorization");

		if (StringUtils.hasText(tokenValue)) {
			tokenValue = jwtUtil.substringToken(tokenValue);
			log.info("토큰 값 " + tokenValue);

			if (!jwtUtil.validateToken(tokenValue)) {
				log.error("Token Error");
				return;
			}

			Claims info = jwtUtil.getUserInfoFromToken(tokenValue);
			log.info("정보 : " + info.getSubject());

			try {
				setAuthentication(info.get("email", String.class));
				log.info("여기 까지 완료 ");
			} catch (Exception e) {
				log.error(e.getMessage());
				return;
			}

		}

		log.info("필터 전 호출 ");
		filterChain.doFilter(request, response);
		log.info("필터 후 호출 ");

	}

	// 인증 처리
	public void setAuthentication(String username) {

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		Authentication authentication = createAuthentication(username);
		context.setAuthentication(authentication);
		
		log.info("유저 인증 정보" + context.getAuthentication());

		SecurityContextHolder.setContext(context);

	}

	// 인증 객체 생성
	private Authentication createAuthentication(String username) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}
}
