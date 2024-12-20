package com.blog_app.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.blog_app.Security.JwtTokenHelper;
import com.blog_app.ServiceImpl.FileServiceimpl;
import com.blog_app.entities.User;
import com.blog_app.exceptions.ApiException;
import com.blog_app.exceptions.PostUpdateDataNotFoundException;
import com.blog_app.payloads.ApiResponse;
import com.blog_app.payloads.JwtAuthRequest;
import com.blog_app.payloads.JwtAuthResponse;
import com.blog_app.payloads.TempUserCache;
import com.blog_app.payloads.UserDto;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.EmailService;
import com.blog_app.services.OTPService;
import com.blog_app.services.TokenBlacklistService;
import com.blog_app.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	
	@Autowired
    private RestTemplate restTemplate;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private JwtTokenHelper jwtTokenHelper;

	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private OTPService otpService;
	
	@Autowired
	private FileServiceimpl fileServiceimpl;
	
	@Autowired
	private TokenBlacklistService tokenBlacklistService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private TempUserCache tempUserCache;
	
	
	
	 private boolean isOAuthUser(User user) {
	        return user.getOauthProvider() != null && user.getOauthProvider().equalsIgnoreCase("GOOGLE");
	    }
	
	@PostMapping("/login")
	public ResponseEntity<JwtAuthResponse> createToken(
			@RequestBody JwtAuthRequest request
			) throws Exception{
		
		this.authenticate(request.getUsername(),request.getPassword());
		
		 UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getUsername());
		String token = this.jwtTokenHelper.generateToken(userDetails);
		String username = this.jwtTokenHelper.getUsernameFromToken(token);
		Optional<User> byEmail = this.userRepo.findByUsernameOrEmail(request.getUsername(),request.getUsername());
		
		JwtAuthResponse response = new JwtAuthResponse();
		response.setToken(token);
	    response.setUserId(this.jwtTokenHelper.getUserIdFromToken(token)); // Extract userId from token
		response.setEmail(this.jwtTokenHelper.getEmailFromToken(token));
		response.setUsername(this.jwtTokenHelper.getUsernameFromToken(token));
		return new ResponseEntity<JwtAuthResponse> (response,HttpStatus.OK);
	}

	private void authenticate(String username, String password) throws Exception {
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,password); 

		try {
			this.authenticationManager.authenticate(authenticationToken);

		} catch (BadCredentialsException e) {
			System.out.println("invalid details !!!");
			throw new ApiException("invalid username or password!!!");
			
		}
		
	}
	@PostMapping("/register")
	public ResponseEntity<String> createUser(@RequestBody UserDto userDto) 
			
	{
		// Check if email or username is already in use
	    if (this.userRepo.existsByEmail(userDto.getEmail())) {
	        throw new PostUpdateDataNotFoundException("Email is already in use. Please use a different email.");
	    }
	    if (this.userRepo.existsByUsername(userDto.getUsername())) {
	        throw new PostUpdateDataNotFoundException("Username is already taken. Please choose a different username.");
	    }		
	    
	    tempUserCache.put(userDto.getEmail(), userDto);

	    
		// Generate OTP
        String otp = otpService.generateOtp(userDto.getEmail());

        // Send OTP via email
        emailService.sendEmail(userDto.getEmail(), "OTP Verification for Activating Your Account ", "Your OTP is: " + otp);

		return new ResponseEntity<>("OTP sent successfully. Please verify to complete registration.", HttpStatus.OK);
	}


	@PostMapping("/verify-otp")
	public ResponseEntity<String> verifyOtp(
			@RequestBody Map<String, String> otpRequest
				) {
		String email = otpRequest.get("email");
		String otp = otpRequest.get("otp");

		if (otpService.validateOtp(email, otp)) {
			
	        UserDto userDto = tempUserCache.get(email);
	        
	        if (userDto != null) {

	        	this.userService.createusertWithImage(userDto);
			// Clear OTP and temp data
            otpService.clearOtp(email);
            tempUserCache.remove(email);
            return new ResponseEntity<>("OTP verified, account created. Please upload profile picture.", HttpStatus.OK);
	        } else {
	            return new ResponseEntity<>("No user data found. Please restart registration.", HttpStatus.BAD_REQUEST);
	        }
	    } else {
	        return new ResponseEntity<>("Invalid OTP. Please try again.", HttpStatus.BAD_REQUEST);
	    }
	}
	
	@PostMapping("/upload-profile-pic")
	public ResponseEntity<JwtAuthResponse> uploadProfilePic(
	        @RequestParam("email") String email,
	        @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

	    User user = userRepo.findByEmail(email)
	            .orElseThrow(() -> new PostUpdateDataNotFoundException("User not found."));

	    if (image != null && !image.isEmpty()) {
	        // Upload provided image
	        String fileName = fileServiceimpl.uploadImage(image);
	        user.setProfilepic(fileName);
	    } else {
	        // Set default image if user skips
	        user.setProfilepic("https://bewbew-images-bucket.s3.amazonaws.com/profile_pic.jfif");
	    }

	    userRepo.save(user);
	    
	    //Step 6: Generate a JWT token for the user
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(user.getEmail());
        String token = this.jwtTokenHelper.generateToken(userDetails);

        // Step 7: Return the JWT token
        JwtAuthResponse jwtResponse = new JwtAuthResponse();
        jwtResponse.setToken(token);
        jwtResponse.setEmail(this.jwtTokenHelper.getEmailFromToken(token));
        jwtResponse.setUserId(this.jwtTokenHelper.getUserIdFromToken(token));
        jwtResponse.setUsername(this.jwtTokenHelper.getUsernameFromToken(token));
        

	    return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
	}
	
	@PostMapping("/edit-email")
	public ResponseEntity<String> editEmail(
	        @RequestParam("oldEmail") String oldEmail,
	        @RequestParam("newEmail") String newEmail) {

	    // Check if the new email is already in use
	    if (userRepo.existsByEmail(newEmail)) {
	        return new ResponseEntity<>("New email is already in use. Please choose a different email.", HttpStatus.BAD_REQUEST);
	    }

	    // Retrieve user data from the cache using the old email
	    UserDto userDto = tempUserCache.get(oldEmail);
	    if (userDto == null) {
	        return new ResponseEntity<>("No user data found for the provided email. Please restart registration.", HttpStatus.BAD_REQUEST);
	    }

	    // Update the email in the cached user data
	    userDto.setEmail(newEmail);
	    tempUserCache.remove(oldEmail);  // Remove old email from cache
	    tempUserCache.put(newEmail, userDto);  // Add new email to cache

	    // Invalidate the old OTP and generate a new one
	    otpService.clearOtp(oldEmail);
	    String newOtp = otpService.generateOtp(newEmail);

	    // Send OTP to the new email
	    emailService.sendEmail(newEmail, "OTP Verification for Activating Your Account", "Your new OTP is: " + newOtp);

	    return new ResponseEntity<>("Email updated and new OTP sent to the new email address.", HttpStatus.OK);
	}

	@PostMapping("/resend-otp")
  public ResponseEntity<String> resendOtp(@RequestParam("email") String email) {
		
		// Check if email is in the registration process (exists in temp cache)
	    UserDto userDto = tempUserCache.get(email);
	    if (userDto == null) {
	        return new ResponseEntity<>("No registration data found for this email. Please start the registration process again.", HttpStatus.BAD_REQUEST);
	    }
	    
	    
		 otpService.clearOtp(email);

      String otp = otpService.generateOtp(email);
      // Assuming you have a method to send OTP via email
      emailService.sendEmail(email, "otp for your email verification", otp);

      return new ResponseEntity<>("A new OTP has been sent to your email.", HttpStatus.OK);
  }

	
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse> logout(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication != null && authentication.getCredentials() != null) {
			String token = (String) authentication.getCredentials();
			
			// Check if user is authenticated with OAuth (Google)
	        User user = (User) authentication.getPrincipal(); // Cast authentication principal to your User class
	        if (isOAuthUser(user)) {
	            String revokeUrl = "https://accounts.google.com/o/oauth2/revoke?token=" + token;
	            restTemplate.postForEntity(revokeUrl, null, String.class);  // Revoke token from Google
	        }
			
			tokenBlacklistService.blacklistToken(token);
			
			SecurityContextHolder.clearContext();
			
			return new ResponseEntity<ApiResponse>(new ApiResponse("logged out",true),HttpStatus.OK);
			
			}else {
				
				return new ResponseEntity<ApiResponse>(new ApiResponse("No user is currently logged in",false),HttpStatus.OK);
	
			}
	}


}
