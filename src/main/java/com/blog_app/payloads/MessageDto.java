package com.blog_app.payloads;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {
    private int senderId;
    private int receiverId;
    private String content;
}
