package com.blog_app.services;

import com.blog_app.entities.FollowRequest;

public interface FollowService {

	FollowRequest sendFollowRequest(int senderId, int receiverId);
	
	void acceptFollowRequest(int requestId);
	
	void declineFollowRequest(int requestId);	
	
	void removeFollower(int userId, int followerId);
	
	void unfollowUser(int userId, int followingId);
}
