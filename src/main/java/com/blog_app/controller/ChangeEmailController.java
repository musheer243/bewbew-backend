package com.blog_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.entities.User;
import com.blog_app.exceptions.SecurityResourceNotFoundException;
import com.blog_app.payloads.ApiResponse;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.EmailService;
import com.blog_app.services.OTPService;

@RestController
@RequestMapping("/api/change-email")
public class ChangeEmailController {
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private OTPService otpService;
	
	@Autowired
	private EmailService emailService;

	 // Step 1: Generate and send OTP to the new email
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOtpForEmailChange(@RequestParam String currentEmail, @RequestParam String newEmail) {
        // Check if the user exists with the current email
        User user = userRepo.findByEmail(currentEmail)
                .orElseThrow(() -> new SecurityResourceNotFoundException("email", "email Id", currentEmail));
        
        // Generate OTP for the new email
        String otp = otpService.generateOtp(newEmail);

        // Send OTP to the new email address
        emailService.sendEmail(newEmail, "Email change OTP", "Your OTP for email change is: " + otp);

        return new ResponseEntity<>(new ApiResponse("OTP sent to your new email.", true), HttpStatus.OK);
    }

    // Step 2: Verify the OTP and update the email
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtpAndChangeEmail(
            @RequestParam String currentEmail,
            @RequestParam String newEmail,
            @RequestParam String otp
    ) {
        // Check if the user exists with the current email
        User user = userRepo.findByEmail(currentEmail)
                .orElseThrow(() -> new SecurityResourceNotFoundException("email", "email Id", currentEmail));

        // Validate OTP for the new email
        boolean isValid = otpService.validateOtp(newEmail, otp);
        if (!isValid) {
            return new ResponseEntity<>(new ApiResponse("Invalid or expired OTP!", false), HttpStatus.UNAUTHORIZED);
        }

        // Clear OTP after verification
        otpService.clearOtp(newEmail);

        // Update the user's email to the new one
        user.setEmail(newEmail);
        userRepo.save(user);

        return new ResponseEntity<>(new ApiResponse("Email updated successfully.", true), HttpStatus.OK);
    }
}
