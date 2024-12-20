package com.blog_app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.entities.Notification;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.repositories.NotificationRepo;

@RestController
@RequestMapping("/api")
public class NotificationController {
	
	@Autowired
	
	private NotificationRepo notificationRepo;

	@PostMapping("/notifications/mark-as-read/{userId}")
	public ResponseEntity<Void> markAsRead(@PathVariable int userId) {
	    List<Notification> notifications = notificationRepo.findAllByReceiverId(userId);
	    notifications.forEach(notification -> notification.setRead(true));
	    notificationRepo.saveAll(notifications);
	    return ResponseEntity.ok().build();
	}
	
	
	@PostMapping("/notifications/single/mark-as-read/{notificationId}")
	public ResponseEntity<Void> markSingleNotificationAsRead(@PathVariable int notificationId) {
	    Notification notification = notificationRepo.findById(notificationId)
	        .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
	    
	    notification.setRead(true);  // Mark the single notification as read
	    notificationRepo.save(notification);
	    
	    return ResponseEntity.ok().build();
	}

	
	
	@GetMapping("/notifications/{userId}")
	public ResponseEntity<List<Notification>> getNotifications(@PathVariable int userId) {
	    List<Notification> notifications = notificationRepo.findAllByReceiverId(userId);
	    return ResponseEntity.ok(notifications);
	}

}
