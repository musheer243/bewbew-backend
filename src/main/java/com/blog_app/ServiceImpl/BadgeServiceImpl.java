package com.blog_app.ServiceImpl;

import org.springframework.stereotype.Service;

import com.blog_app.services.BadgeService;
@Service
public class BadgeServiceImpl implements BadgeService {

	@Override
	public String getBadgeForPostCount(int totalPosts) {
		
		if (totalPosts>100) {
			return "Expert";
		}else if (totalPosts>50) {
			return "Advanced";
		}else if (totalPosts>10) {
			return "Intermediate";
		}else {
			return "Beginner";
		}
		
	}

}
