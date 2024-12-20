package com.blog_app.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.entities.NotInterestedPost;
import com.blog_app.entities.Post;
import com.blog_app.entities.User;
import com.blog_app.exceptions.PostUpdateDataNotFoundException;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.payloads.ApiResponse;
import com.blog_app.payloads.PostDto;
import com.blog_app.repositories.NotInterestedPostRepo;
import com.blog_app.repositories.PostRepo;
import com.blog_app.repositories.UserRepo;

@RestController
@RequestMapping("/api/post")
public class NotInterestedPostController {

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private NotInterestedPostRepo notInterestedPostRepo;
	
	@PostMapping("/not-interested/{postId}")
	public ResponseEntity<ApiResponse> markPostAsNotInterested(
			@PathVariable Integer postId,
			@RequestParam Integer userId){
		
		Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResourceNotFoundException("post", "postId", postId));
		
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		
		// Check if the post is already marked as "not interested" by the user
	    Optional<NotInterestedPost> byUserAndPost = this.notInterestedPostRepo.findByUserAndPost(user, post);
	    
	    if (byUserAndPost.isPresent()) {
	        // If the post is already marked as "not interested," remove it
	        this.notInterestedPostRepo.delete(byUserAndPost.get());
	        return new ResponseEntity<>(new ApiResponse("Post removed from 'Not Interested'", true), HttpStatus.OK);
	    }else {
		
		NotInterestedPost notInterestedPost = new NotInterestedPost();
		notInterestedPost.setUser(user);
	    notInterestedPost.setPost(post);
	    notInterestedPost.setMarkedDate(LocalDateTime.now());

	    notInterestedPostRepo.save(notInterestedPost);
		
	    return new ResponseEntity<>(new ApiResponse("Post marked as 'Not Interested'", true), HttpStatus.OK);
	    }
	}
	
	@GetMapping("/not-interested/{userId}")
    public ResponseEntity<List<PostDto>> getNotInterestedPosts(@PathVariable Integer userId) {
		 User user = this.userRepo.findById(userId)
			        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

			    // Get all NotInterestedPost entries for the user
			     Set<NotInterestedPost> set = this.notInterestedPostRepo.findByUser(user);

			     List<PostDto> collect = set.stream().map(post-> modelMapper.map(post.getPost(), PostDto.class)).collect(Collectors.toList());
			    

        return new ResponseEntity<List<PostDto>>(collect, HttpStatus.OK);
    }
	
	

}
