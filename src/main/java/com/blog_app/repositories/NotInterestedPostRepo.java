package com.blog_app.repositories;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.blog_app.entities.NotInterestedPost;
import com.blog_app.entities.Post;
import java.util.List;
import java.util.Optional;

import com.blog_app.entities.User;


public interface NotInterestedPostRepo extends JpaRepository<NotInterestedPost, Integer> {
	
	Set<NotInterestedPost> findByUser(User user);
	
    boolean existsByUserAndPost(User user, Post post);

    // New method to directly fetch Posts that the user has marked as "Not Interested"
    @Query("SELECT nip.post FROM NotInterestedPost nip WHERE nip.user = :user")
    Set<Post> findPostByUser(@Param("user") User user);

	Optional<NotInterestedPost> findByUserAndPost(User user,Post post);
    
    }
