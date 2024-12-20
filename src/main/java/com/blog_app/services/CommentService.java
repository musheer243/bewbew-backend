package com.blog_app.services;

import java.util.List;

import com.blog_app.payloads.CommentDto;

public interface CommentService {

	//create
	CommentDto createComment(CommentDto commentDto , Integer userId, Integer postId);
	
	//update
	CommentDto updateComment(CommentDto commentDto, Integer commentId);
	
	//delete
	void deleteComment(Integer commentId);
	
	
    List<CommentDto> getCommentsByPost(Integer postId);

}