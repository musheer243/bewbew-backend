package com.blog_app.ServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog_app.entities.User;
import com.blog_app.exceptions.PostUpdateDataNotFoundException;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.exceptions.SecurityResourceNotFoundException;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.OTPService;

@Service
public class OTPServiceImpl implements OTPService {
	
	@Autowired
	private UserRepo userRepo;
	
	private final Map<String, String> otpStorage = new HashMap<>();
	private final long OTP_VALIDITY = 5 * 60 * 100;

	@Override
	public String generateOtp(String email) {
		//User user = this.userRepo.findByEmail(email).orElseThrow(()-> new PostUpdateDataNotFoundException("email: " + email + " doesnt exist in database"));
		
	    String otp = String.format("%06d", new Random().nextInt(999999));
		otpStorage.put(email, otp);
		
		return otp;
	}

	@Override
	public boolean validateOtp(String email, String otp) {
		return otp.equals(otpStorage.get(email));
	}

	@Override
	public void clearOtp(String email) {
		otpStorage.remove(email);
		
		
		
	}
	


}
