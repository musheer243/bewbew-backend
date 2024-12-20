package com.blog_app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.Post;
import com.blog_app.entities.PostLike;
import com.blog_app.entities.SavedPost;
import com.blog_app.entities.User;

public interface SavedPostRepo extends JpaRepository<SavedPost, Integer> {

	    boolean existsByPostAndUser(Post post,User user);
		
		SavedPost findByPostAndUser(Post post, User user);

		Page<SavedPost>findAllByUser(User user, Pageable pageable);
}
