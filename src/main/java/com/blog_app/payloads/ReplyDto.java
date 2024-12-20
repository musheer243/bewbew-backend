package com.blog_app.payloads;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ReplyDto {

	private int id;
    private String content;
    private LocalDateTime date;
    private UserDto user;
//    private CommentDto comment;
}
