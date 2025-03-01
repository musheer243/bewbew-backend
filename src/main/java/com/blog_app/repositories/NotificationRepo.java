package com.blog_app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.Notification;
import com.blog_app.entities.User;

public interface NotificationRepo extends JpaRepository<Notification, Integer>{
	
	// Custom query method to find notifications by the receiver's userId
    List<Notification> findAllByReceiverId(int receiverId);

	Notification findBySenderAndReceiverAndPostIdAndRedirectUrl(User user, User user2, int postId, String string);

	Notification findBySenderAndReceiverAndRedirectUrl(User sender, User receiver, String string);

	


}
