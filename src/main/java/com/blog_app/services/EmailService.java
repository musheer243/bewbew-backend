package com.blog_app.services;

public interface EmailService {

	void sendEmail(String to, String subject, String body);
}
