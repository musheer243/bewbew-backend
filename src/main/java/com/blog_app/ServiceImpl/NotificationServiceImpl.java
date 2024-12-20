package com.blog_app.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.blog_app.entities.Notification;

@Service
public class NotificationServiceImpl {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(Notification notification) {
        // Sending notification to the user's WebSocket channel
       // System.out.println("Sending notification to: " + notification.getReceiver().getUsername());
    	//messagingTemplate.convertAndSendToUser("bilal_khan", "/topic/notifications", "Test notification");

        messagingTemplate.convertAndSendToUser(notification.getReceiver().getName(), "/topic/notifications", notification
        );
        System.out.println("Sending notification to: " + notification.getReceiver().getName());

    }
}
