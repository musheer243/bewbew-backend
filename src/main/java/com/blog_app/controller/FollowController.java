package com.blog_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.entities.FollowRequest;
import com.blog_app.services.FollowService;

@RestController
@RequestMapping("/api/follow")
public class FollowController {
 
	@Autowired
	private FollowService followService;

	 @PostMapping("/send/{senderId}/{receiverId}")
	public ResponseEntity<String> sendFollowRequest(@PathVariable int senderId, @PathVariable int receiverId){
	
		 FollowRequest sendFollowRequest = this.followService.sendFollowRequest(senderId, receiverId);
		
		 if (sendFollowRequest == null) {
			
			 return new ResponseEntity<String>("Followed successfully",HttpStatus.OK);
		}else {
			
			return new ResponseEntity<String>("Follow request sent",HttpStatus.OK);
		}	
	}
	 
	 @PostMapping("/accept/{requestId}")
	 public ResponseEntity<String> acceptFollowRequest(@PathVariable int requestId){
		 
		 this.followService.acceptFollowRequest(requestId);
		 	
		 return new ResponseEntity<String>("Follow request accepted",HttpStatus.OK);
	 }
	 
	    @PostMapping("/decline/{requestId}")
	    public ResponseEntity<String> declineFollowRequest(@PathVariable int requestId){
	    	
	    	this.followService.declineFollowRequest(requestId);
	    	return new ResponseEntity<String>("Follow request declined",HttpStatus.OK);
	    			
	    }
	    
	 // Remove a follower
	    @PostMapping("/{userId}/remove-follower/{followerId}")
	    public ResponseEntity<String> removeFollower(@PathVariable int userId, @PathVariable int followerId) {
	        followService.removeFollower(userId, followerId);
	        return ResponseEntity.ok("Follower removed successfully");
	    }

	    // Unfollow a user
	    @PostMapping("/{userId}/unfollow/{followingId}")
	    public ResponseEntity<String> unfollowUser(@PathVariable int userId, @PathVariable int followingId) {
	        followService.unfollowUser(userId, followingId);
	        return ResponseEntity.ok("Unfollowed user successfully");
}
}
