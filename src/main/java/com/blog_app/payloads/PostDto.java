package com.blog_app.payloads;


import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import java.util.Date;

import javax.swing.text.AbstractDocument.Content;

import org.hibernate.bytecode.internal.bytebuddy.PrivateAccessorException;

import com.blog_app.entities.Category;
import com.blog_app.entities.Comment;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostDto {
	
	private int postId;

	private String title;
	
	private String content;
	
	private List<String> mediaFileNames;
	
    private List<String> keptOldLinks; // <--- Add this

	
	private LocalDateTime addedDate;
	
	private int likeCount = 0;
	
	private int saveCount = 0;
	
	private int commentCount = 0;
	
	// Add this field to capture the scheduled date
    private LocalDateTime scheduledDate; 
    
    private boolean closeFriendsOnly = false;

    
	private boolean isPublished = false;
	
	private CategoryDto category;
	
	private UserDto user;
	
	
	
	}
