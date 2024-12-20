package com.blog_app.payloads;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.blog_app.entities.Post;
import com.blog_app.entities.Reply;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String content;
	
	private LocalDateTime date;
	
	private UserDto user;
	
	private PostDto post;
	
    private List<ReplyDto> replies;

	
}
