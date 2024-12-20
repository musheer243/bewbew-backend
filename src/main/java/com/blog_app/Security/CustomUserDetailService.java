package com.blog_app.Security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.blog_app.entities.User;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.exceptions.SecurityResourceNotFoundException;
import com.blog_app.repositories.UserRepo;

@Service
public class CustomUserDetailService implements UserDetailsService{

	@Autowired
	private UserRepo userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

		//Loading User from Database by UserName
		 User user = this.userRepo.findByUsernameOrEmail(usernameOrEmail,usernameOrEmail).orElseThrow(()->  new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
 
		
		return user;
	}
	
	
	

}
