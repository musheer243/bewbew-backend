package com.blog_app.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Table(name = "follow_requests")
@Entity
@NoArgsConstructor
@Setter
@Getter

public class FollowRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	 @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "sender_id")
	private User sender;
	
	 @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "receiver_id")
	private User receiver;
	
	private LocalDateTime sentAt;
	
	private String status;
	
}
