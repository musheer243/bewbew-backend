package com.blog_app.services;

import java.util.List;

import com.blog_app.entities.SavedPost;
import com.blog_app.payloads.PostDto;
import com.blog_app.payloads.PostResponse;

public interface SavedPostService {

	String toggleSavedPost(int postId, int userId);
	
    PostResponse getSavedPostsByUser(int userId,int pageNumber, int pageSize, String sortBy, String sortDir);

	
	}
