package com.blog_app.repositories;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.Comment;
import com.blog_app.entities.Post;

public interface CommentRepo extends JpaRepository<Comment, Integer>{

	Page<Comment> findAllByPost(Post post, Pageable pageable);

}
