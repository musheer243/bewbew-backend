package com.blog_app.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecurityResourceNotFoundException extends RuntimeException{

	String resourceName;
	String fieldName;
	String fieldValue;
	
	public SecurityResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
		super(String.format("%s not found with %s : %s",resourceName,fieldName,fieldValue));  //constructor
		this.resourceName = resourceName;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}
}
