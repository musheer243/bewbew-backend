package com.blog_app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.BlockedUser;
import com.blog_app.entities.User;

public interface BlockedUserRepo extends JpaRepository<BlockedUser, Integer> {

	Optional<BlockedUser> findByBlockedByAndBlockedUser(User blockedBy, User blockedUser);

}
