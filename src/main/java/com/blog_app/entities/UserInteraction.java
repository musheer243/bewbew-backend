package com.blog_app.entities;

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
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_interaction")
public class UserInteraction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
	private Post post;
	
	private int interactionType; // 1 for like and 2 for save
	
	private double interactionScore; // 1 for like and 2 for save
	
}
