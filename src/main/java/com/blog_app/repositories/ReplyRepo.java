package com.blog_app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.Comment;
import com.blog_app.entities.Reply;

public interface ReplyRepo extends JpaRepository<Reply, Integer> {
	
    List<Reply> findAllByComment(Comment comment);


}
