package com.blog_app.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostUpdateDataNotFoundException extends RuntimeException{

    public PostUpdateDataNotFoundException(String message) {
        super(message);  // Pass the message directly to the parent class
    }
}
