package com.blog_app.services;

import java.util.List;

import com.blog_app.entities.Post;
import com.blog_app.entities.User;

public interface RecommendationService {

	List<Post> recommendPosts(User user, int howMany) throws Exception;

	List<Post> getRecommendationsForUser(User user, int trendingLimit, int categoryLimit);
}
