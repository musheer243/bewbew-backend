package com.blog_app.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.blog_app.payloads.CommentDto;

public interface CommentService {

	//create
	CommentDto createComment(CommentDto commentDto , Integer userId, Integer postId);
	
	//update
	CommentDto updateComment(CommentDto commentDto, Integer commentId);
	
	//delete
	void deleteComment(Integer commentId);
	

	Page<CommentDto> getCommentsByPost(Integer postId, int page, int size);

}