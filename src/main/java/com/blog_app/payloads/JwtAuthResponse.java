package com.blog_app.payloads;

import lombok.Data;

@Data
public class JwtAuthResponse {
	
	private String token;
	
	private int userId;
	
	private String email;
	
	private String username;
	

}