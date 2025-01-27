package com.blog_app.services;

import java.util.List;

import com.blog_app.payloads.PostDto;
import com.blog_app.payloads.PostResponse;
import com.blog_app.payloads.UserDto;

public interface PostLikeService {

	String toggleLikePost(int postId, int userId);
	
	boolean isPostLikedByUser(int postId, int userId);
	    
    List<UserDto> getUsersWhoLikedPost(int postId);

    PostResponse getLikedPostsByUser(int userId,int pageNumber, int pageSize, String sortBy, String sortDir);
}
