package com.blog_app.services;

public interface TokenBlacklistService {

	void blacklistToken(String token);
	
	boolean isTokenBlacklisted(String token);
}
