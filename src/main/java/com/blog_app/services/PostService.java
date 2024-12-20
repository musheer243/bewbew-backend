package com.blog_app.services;

import java.io.IOException;
import java.util.List;

import org.hibernate.query.Page;
import org.springframework.web.multipart.MultipartFile;

import com.blog_app.entities.Post;
import com.blog_app.payloads.PostDto;
import com.blog_app.payloads.PostResponse;

public interface PostService {

	

	//delete
	void deletePost(Integer postId);
	
	//get all Post
	PostResponse getAllPost(int pageNumber, int pageSize, String sortBy, String sortDir);

	//get single Post
	PostDto getPostById(Integer postId);
	
	//get all post by category
	PostResponse getPostByCategory(Integer categoryId, int pageNumber, int pageSize, String sortBy, String sortDir);
	
	//get all post by users
	PostResponse getPostByUser(Integer userId, int pageNumber, int pageSize, String sortBy, String sortDir);
	
	///search Post
	List<PostDto> searchPosts(String keyword);

		
	PostDto createPostWithMedia(String postDto, Integer userId, Integer categoryId, List<MultipartFile> files)
			throws IOException;

	PostDto updatePostWithMedia(Integer postId, String postDto, List<MultipartFile> files)throws IOException;
	
	//search post by categpory
	PostResponse getPostsByCategoryTitle(String categoryTitle,int pageNumber, int pageSize, String sortBy, String sortDir);
	
	//get all scheduled posts
	List<PostDto> getScheduledPosts();

	//get scheduled posts by user
	List<PostDto> getScheduledPostsForUser(Integer userId);
	
	PostResponse getPostsByUsersFollowing(Integer userId, int pageNumber, int pageSize, String sortBy, String sortDir);
	
	
}
