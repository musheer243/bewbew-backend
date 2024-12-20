package com.blog_app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.entities.Comment;
import com.blog_app.payloads.ApiResponse;
import com.blog_app.payloads.CommentDto;
import com.blog_app.payloads.PostDto;
import com.blog_app.services.CommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/")
public class CommentController {
	
	@Autowired
	private CommentService commentService; 

	@PostMapping("/post/{postId}/user/{userId}/comments/")
	public ResponseEntity<CommentDto> createComment(
			@RequestBody CommentDto commentDto, 
			@PathVariable Integer userId,
			@PathVariable Integer postId
			){
		
	
		CommentDto createComment = this.commentService.createComment(commentDto,userId, postId );

		return new ResponseEntity<CommentDto> (createComment,HttpStatus.CREATED);
		
	}
	
	@DeleteMapping("/comment/{commentId}")
	public ApiResponse deleteComment(@PathVariable Integer commentId) {
		
		this.commentService.deleteComment(commentId);
	    return new ApiResponse("Comment is successfully deleted", true);
		
	}
	
	//update 
			@PutMapping("/comment/update/{commentId}")
			public ResponseEntity<CommentDto> updateComment(@Valid @RequestBody CommentDto commentDto,@PathVariable Integer commentId)
			{
				 CommentDto updatedComment = this.commentService.updateComment(commentDto, commentId);
				return new ResponseEntity<CommentDto>(updatedComment,HttpStatus.OK);
				
			}
			
			//get comment by post
			 @GetMapping("/comment/post/{postId}")
			    public ResponseEntity<List<CommentDto>> getCommentsByPost(@PathVariable Integer postId) {
			        List<CommentDto> comments = commentService.getCommentsByPost(postId);
			        return ResponseEntity.ok(comments);
			    }
	
}
