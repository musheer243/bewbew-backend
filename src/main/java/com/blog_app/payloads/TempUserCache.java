package com.blog_app.payloads;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class TempUserCache {
	
	private Map<String,UserDto> userCache = new HashMap<>();
	
	 public void put(String email, UserDto userDto) {
	        userCache.put(email, userDto);
	    }

	    public UserDto get(String email) {
	        return userCache.get(email);
	    }

	    public void remove(String email) {
	        userCache.remove(email);
	    }

}
