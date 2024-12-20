package com.blog_app.ServiceImpl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.blog_app.services.TokenBlacklistService;
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

	
	private Set<String> blackListedTokens = new HashSet<>();
	
	@Override
	public void blacklistToken(String token) {
		blackListedTokens.add(token);
	}

	@Override
	public boolean isTokenBlacklisted(String token) {
		return blackListedTokens.contains(token);
	}

}
