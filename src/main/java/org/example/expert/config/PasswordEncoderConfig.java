package org.example.expert.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PasswordEncoderConfig implements PasswordEncoder {

	private final BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();

	private final DelegatingPasswordEncoder delegatingPasswordEncoder;

	public PasswordEncoderConfig() {
		// 사용할 암호화 방식으로 bcrypt를 설정
		Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put("bcrypt", bcryptPasswordEncoder);

		// DelegatingPasswordEncoder 생성
		delegatingPasswordEncoder = new DelegatingPasswordEncoder("bcrypt", encoders);

		// 기본 암호화 방식을 bcrypt로 설정
		delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(bcryptPasswordEncoder);
	}

	@Override
	public String encode(CharSequence rawPassword) {
		// 기존의 BCrypt 로직을 그대로 사용할 수 있게 하면서, delegatingPasswordEncoder의 encode 메서드를 사용
		return delegatingPasswordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		// 기존의 BCrypt 로직을 그대로 사용하면서, delegatingPasswordEncoder의 matches 메서드를 사용
		return delegatingPasswordEncoder.matches(rawPassword, encodedPassword);
	}
}
