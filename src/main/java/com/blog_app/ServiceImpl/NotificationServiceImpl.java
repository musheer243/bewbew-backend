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
       
    	String username = notification.getReceiver().getUsername();
    	messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification);

        System.out.println("Sent notification to user ID: " + username);
        System.out.println("Notification data: " + notification);


    }
}
