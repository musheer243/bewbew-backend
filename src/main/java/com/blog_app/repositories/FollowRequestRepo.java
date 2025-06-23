package com.blog_app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.FollowRequest;
import com.blog_app.entities.User;

import jakarta.validation.constraints.AssertFalse.List;

public interface FollowRequestRepo extends JpaRepository<FollowRequest, Integer> {

	FollowRequest findBySenderIdAndReceiverIdAndStatus(int viewerId, int userId, String string);

	java.util.List<FollowRequest> findAllByReceiverIdAndStatus(int receiverId, String status);



}
