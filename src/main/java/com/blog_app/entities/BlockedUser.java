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
@Table(name = "blocked_users")
@Getter
@Setter
@NoArgsConstructor
public class BlockedUser {
	
	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private int id;

	    @ManyToOne
	    @JoinColumn(name = "blocked_by", nullable = false)
	    private User blockedBy;

	    @ManyToOne
	    @JoinColumn(name = "blocked_user", nullable = false)
	    private User blockedUser;

}
