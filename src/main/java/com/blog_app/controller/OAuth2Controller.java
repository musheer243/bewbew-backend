package com.blog_app.controller;

import java.io.Console;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import com.blog_app.Security.JwtTokenHelper;
import com.blog_app.config.AppConstants;
import com.blog_app.entities.Role;
import com.blog_app.entities.User;
import com.blog_app.payloads.JwtAuthResponse;
import com.blog_app.repositories.RoleRepo;
import com.blog_app.repositories.UserRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/oauth2")
public class OAuth2Controller {
	
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private RoleRepo roleRepo;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private JwtTokenHelper jwtTokenHelper;



	 	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	    private String clientId;

	    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
	    private String clientSecret;

	    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
	    private String redirectUri;
	    
	    private final String AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
	    private final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
	    private final String USER_INFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";

	 // Step 1: Redirect user to Google OAuth2 login
	    @GetMapping("/google/login")
	    public RedirectView redirectToGoogle() {
	        String googleLoginUrl = AUTHORIZATION_ENDPOINT + "?client_id=" + clientId +
	                "&redirect_uri=" + redirectUri + "&response_type=code&scope=email%20profile&access_type=offline";
	        return new RedirectView(googleLoginUrl);
	    }
	    
	 // Step 2: Handle Google's callback and exchange the authorization code for an access token
	    @GetMapping("/google/callback")
	    public RedirectView handleGoogleCallback(@RequestParam("code") String code) throws JsonProcessingException {
	        // Exchange the authorization code for an access token
	       // String extractedCode = code;
//	        
//	        String extractedCode = code.split("&")[0];
//
//	        String remainingCode = code.substring(extractedCode.length() + 1);  // +1 to remove the '&'
//
//	     // Now, `extractedCode` contains the part you wanted to extract
//	        System.out.println("Extracted code: " + extractedCode);
//	        System.out.println("Remaining code: " + remainingCode);

	        MultiValueMap<String, String> tokenRequestParams = new LinkedMultiValueMap<>();
	        tokenRequestParams.add("code", code);
	        tokenRequestParams.add("client_id", clientId);
	        tokenRequestParams.add("client_secret", clientSecret);
	        tokenRequestParams.add("redirect_uri", redirectUri);
	        tokenRequestParams.add("grant_type", "authorization_code");

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenRequestParams, headers);

	        // Step 3: Get the access token from Google
	        ResponseEntity<String> response = restTemplate.exchange(TOKEN_ENDPOINT, HttpMethod.POST, tokenRequest, String.class);
	        ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode responseJson = objectMapper.readTree(response.getBody());
	        String accessToken = responseJson.get("access_token").asText();

	        // Step 4: Use access token to fetch user details
	        HttpHeaders userInfoHeaders = new HttpHeaders();
	        userInfoHeaders.setBearerAuth(accessToken);
	        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

	        ResponseEntity<String> userInfoResponse = restTemplate.exchange(USER_INFO_ENDPOINT, HttpMethod.GET, userInfoRequest, String.class);
	        JsonNode userInfoJson = objectMapper.readTree(userInfoResponse.getBody());

	        String email = userInfoJson.get("email").asText();
	        String name = userInfoJson.get("name").asText();
	        String picture = userInfoJson.get("picture").asText();

	        // Step 5: Save user details in the database
	        User user = userRepo.findByEmail(email).orElseGet(() -> registerNewUser(email, name, picture));

	        // Step 6: Generate a JWT token for the user
	        UserDetails userDetails = this.userDetailsService.loadUserByUsername(user.getEmail());
	        String token = this.jwtTokenHelper.generateToken(userDetails);

	        // Redirect to frontend with the token https://bewbew.serveblog.net/
	        String frontendRedirectUrl = "https://bewbew.serveblog.net/login-success?token=" + token;
	        return new RedirectView(frontendRedirectUrl);
	    }

	    
	 // Helper method to register new users
	    private User registerNewUser(String email, String name, String profilePic) {
	        User user = new User();
	        user.setEmail(email);
	        user.setName(name);
	        user.setProfilepic(profilePic);
	        user.setJoiningdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
	        user.setVerified(true); // Since Google verifies email
	        user.setTotalPosts(0);
	        user.setTotalLikes(0);
	        
	        user.setOauthProvider("GOOGLE");
	        
	        String uniqueUsername = generateUniqueUsername(name);
	        user.setUsername(uniqueUsername);


	        // Set default role as NORMAL_USER
	        Role role = this.roleRepo.findById(AppConstants.NORMAL_USER).get();
	        user.getRoles().add(role);

	        return userRepo.save(user);
	    }
	    
	 // Method to generate a unique username
	    private String generateUniqueUsername(String name) {
	        String baseUsername = name.replaceAll("\\s+", "").toLowerCase(); // Remove spaces and lowercase
	        String username = baseUsername + getRandomThreeDigitNumber();
	        
	        // Check for uniqueness
	        while (userRepo.findByUsername(username).isPresent()) {
	            username = baseUsername + getRandomThreeDigitNumber(); // Regenerate if not unique
	        }
	        return username;
	    }

	 // Method to get a random three-digit number
	    private String getRandomThreeDigitNumber() {
	        int randomNum = 1 + (int)(Math.random() * 999); // Generates a number between 1 and 999
	        return String.format("%03d", randomNum); // Format to ensure three digits with leading zeros
	    }
}
