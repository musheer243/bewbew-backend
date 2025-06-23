package com.blog_app.payloads;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.blog_app.entities.User;

import jakarta.validation.constraints.Email;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Setter
public class UserDto {

	
	private int id;
	
	@NotEmpty
	@Size(min=3, message = "Please enter minimum 4 letters to validate the name.")
	private String name;
	
	@NotEmpty
	@Size(min = 5,message = "Please enter minimum 5 letters")
	private String username;

	@NotEmpty
	@Email(message="Your eamil address is not valid!")
	private String email;
	
	@NotEmpty
	@Size(min=5 , message = "Password should be minimum 5 char.")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,10}$",message = "Password should contain 1 Upper case Letter, numbers & with special symbol")
	private String password;
	
	
	private String about;
	
	
	private String profilepic;
	
	private LocalDateTime joiningdate;
	
	private String userPreference;
	
    private int totalLikes = 0;

    private int totalPosts = 0;
    
	private String oauthProvider; 

    private String badge = "Beginner";
    
    private boolean isPrivate = false;
    
    private int totalFollowers = 0;

    private int totalFollowings = 0;

    private int unreadCount;

//    private Set<UserDto> followers = new HashSet<>();
//
//    private Set<UserDto> following = new HashSet<>();


}
