package com.blog_app.services;

public interface OTPService {
	
	String generateOtp(String email);
	
	boolean validateOtp(String email, String otp);
	
	void clearOtp(String email);

}
