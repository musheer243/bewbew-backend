package com.blog_app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.FollowRequest;

public interface FollowRequestRepo extends JpaRepository<FollowRequest, Integer> {

}
