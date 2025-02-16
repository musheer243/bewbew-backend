package com.blog_app.payloads;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {
	private int id;
    private int senderId;
    private int receiverId;
    private String content;
    private LocalDateTime sentAt; // Timestamp of the message
    private String senderProfilePic;

}
