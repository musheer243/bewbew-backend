package com.blog_app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.Message;
import com.blog_app.entities.User;

public interface MessageRepo extends JpaRepository<Message, Integer> {
    List<Message> findBySenderAndReceiverOrReceiverAndSender(User sender1, User receiver1, User sender2, User receiver2);

}
