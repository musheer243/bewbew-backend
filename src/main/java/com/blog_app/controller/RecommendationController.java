package com.blog_app.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.config.AppConstants;
import com.blog_app.entities.Post;
import com.blog_app.entities.User;
import com.blog_app.payloads.PostDto;
import com.blog_app.services.RecommendationService;
import com.blog_app.services.UserService;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private RecommendationService recommendationService;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@GetMapping("/byCollaborative")
	public ResponseEntity<List<PostDto>> getRecommendations(@RequestParam(value = "howMany", defaultValue = "10", required = false) Integer howMany) {
		try {
			User currentuser = userService.getLoggedInUser();
			
			List<Post> posts = recommendationService.recommendPosts(currentuser, howMany);
			
			List<PostDto> list = posts.stream().map(post-> modelMapper.map(post, PostDto.class)).collect(Collectors.toList());
			
            return ResponseEntity.ok(list);

			
		} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@GetMapping("/user")
    public ResponseEntity<List<Post>> getRecommendationsForUser(@AuthenticationPrincipal User user) {
        // Define how many trending and category posts you want
        int trendingLimit = 10;
        int categoryLimit = 10;

        // Fetch recommendations
        List<Post> recommendedPosts = recommendationService.getRecommendationsForUser(user, trendingLimit, categoryLimit);

        return ResponseEntity.ok(recommendedPosts);
    }
}
