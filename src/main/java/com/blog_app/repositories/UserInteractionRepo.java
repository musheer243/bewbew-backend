package com.blog_app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.Post;
import com.blog_app.entities.User;
import com.blog_app.entities.UserInteraction;

public interface UserInteractionRepo extends JpaRepository<UserInteraction, Long> {

	List<UserInteraction> findByUser(User user);
	
	List<UserInteraction> findByPost(Post post);
	
	UserInteraction findByPostAndUser(Post post, User user);
	
}
