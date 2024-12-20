package com.blog_app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.Notification;

public interface NotificationRepo extends JpaRepository<Notification, Integer>{
	
	// Custom query method to find notifications by the receiver's userId
    List<Notification> findAllByReceiverId(int receiverId);


}
