package com.blog_app.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.entities.User;
import com.blog_app.payloads.UserDto;
import com.blog_app.services.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/closefriend")
public class CloseFriendController {

	@Autowired
	private UserService userService;
	
//	@PostMapping("/{userId}/{friendId}")
//    public ResponseEntity<String> addCloseFriend(@PathVariable int userId, @PathVariable int friendId) {
//			
//			boolean closeFriend = this.userService.addCloseFriend(userId, friendId);
//		
//        if (closeFriend) {
//            return ResponseEntity.ok("added to Close Friends");
//        } else {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot add user to Close Friends as they are not following you.");
//        }
//    }
	
	@PostMapping("/{userId}")
	public ResponseEntity<String> addMultipleCloseFriends(
	        @PathVariable Integer userId,
	        @RequestParam List<Integer> friendIds) {

	    boolean success = this.userService.addMultipleCloseFriends(userId, friendIds);

	    if (success) {
	        return ResponseEntity.ok("Friends added to close friends successfully");
	    } else {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add close friends.");
	    }
	}

	
	@GetMapping("/{userId}")
	public ResponseEntity<Set<UserDto>> getCloseFriends(@PathVariable Integer userId) {
	    Set<UserDto> closeFriends = userService.getCloseFriends(userId);
	    return ResponseEntity.ok(closeFriends);
	}
	
	@DeleteMapping("/{userId}/{friendId}")
	public ResponseEntity<String> removeCloseFriend(@PathVariable Integer userId, @PathVariable int friendId) {
	    boolean success = userService.removeCloseFriend(userId, friendId);
	    
	    if (success) {
	        return ResponseEntity.ok("Friend removed from Close Friends");
	    } else {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user is not in your Close Friends list.");
	    }
	}
	
}
