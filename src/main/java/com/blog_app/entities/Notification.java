package com.blog_app.entities;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor

public class Notification {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private int id;

	 	@JsonIgnore
	    @ManyToOne
	    @JoinColumn(name = "sender_id") // The user who liked/commented
	    private User sender;

	 	@JsonIgnore
	    @ManyToOne
	    @JoinColumn(name = "receiver_id") // The post owner
	    private User receiver;

	    private String message; // e.g., "User X liked your post", "User Y commented on your post"
	    
	    @Column(name = "is_read")
	    private boolean isRead = false; // Initially unread

	    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Kolkata")
	    private LocalDateTime timestamp; // When the notification was created
	    
	 // Additional fields for post and comment IDs
	    @Column(name = "post_id")
	    private Integer postId;

	    @Column(name = "comment_id")
	    private Integer commentId;
	}
