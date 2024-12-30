package com.blog_app.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.entities.User;
import com.blog_app.exceptions.PostUpdateDataNotFoundException;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.exceptions.SecurityResourceNotFoundException;
import com.blog_app.payloads.ApiResponse;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.EmailService;
import com.blog_app.services.OTPService;

@RestController
@RequestMapping("/api/password")
public class ForgetPasswordController {
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private OTPService otpService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@PostMapping("/forget")
	public ResponseEntity<ApiResponse> forgetPassword(@RequestParam String email){
		//hav to apply checks overhere to check if the user provided email does exists in the database
		String otp = otpService.generateOtp(email);
		emailService.sendEmail(email, "password reset otp valid for 5mins", "your otp is: " + otp);
		return new ResponseEntity<ApiResponse>(new ApiResponse("OTP sent to your email.",true),HttpStatus.OK);
       // return ResponseEntity.ok("OTP sent to your email.");
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<ApiResponse> verifyOtp(
			@RequestParam String email,
			@RequestParam String otp
			){
		
		boolean isValid = otpService.validateOtp(email, otp);
		if (!isValid) {
			return new ResponseEntity<ApiResponse>(new ApiResponse("invalid or otp expired!!!",false),HttpStatus.UNAUTHORIZED);
			//return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid or otp expired!!!");
		}
		otpService.clearOtp(email);
		return new ResponseEntity<ApiResponse>(new ApiResponse("OTP verified, you can now reset your password.",true),HttpStatus.OK);
		//return ResponseEntity.ok("OTP verified, you can now reset your password.");
	}
	
	@PostMapping("/reset-password")
	public ResponseEntity<ApiResponse> resetPassword(
			@RequestParam String email, 
			@RequestParam String newPassword
			){
		User user = this.userRepo.findByEmail(email).orElseThrow(()-> new SecurityResourceNotFoundException("email", "email Id", email));
		if (passwordEncoder.matches(newPassword, user.getPassword())) {
			throw new PostUpdateDataNotFoundException("password cannot be same as previous");
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		this.userRepo.save(user);
		
		return new ResponseEntity<ApiResponse>(new ApiResponse("password reset successfull",true),HttpStatus.OK);
		//return ResponseEntity.ok("password reset successfull");
	}
	
}
