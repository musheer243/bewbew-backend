package com.blog_app.payloads;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FollowRequestDto {
    private int id;
    private int senderId;
    private String senderName;
    private String senderProfilePic;
    private int receiverId;
    private LocalDateTime sentAt;
    private String status;
    private String redirectUrl; // URL to redirect to when the notification is clicked
}
