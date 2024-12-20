package com.blog_app.repositories;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.Post;
import com.blog_app.entities.PostLike;
import com.blog_app.entities.User;

public interface PostLikeRepo extends JpaRepository<PostLike, Integer> {

	boolean existsByPostAndUser(Post post,User user);
	
	PostLike findByPostAndUser(Post post, User user);
    
    Page<PostLike> findAllByUser(User user, Pageable pageable);

    List<PostLike> findAllByPost(Post post);

}
