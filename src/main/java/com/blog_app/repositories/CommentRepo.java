package com.blog_app.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.Comment;
import com.blog_app.entities.Post;

public interface CommentRepo extends JpaRepository<Comment, Integer>{

    List<Comment> findAllByPost(Post post);

}
