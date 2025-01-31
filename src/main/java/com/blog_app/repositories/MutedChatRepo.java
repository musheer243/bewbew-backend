package com.blog_app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.MutedChat;
import com.blog_app.entities.User;

public interface MutedChatRepo extends JpaRepository<MutedChat, Integer>{

	Optional<MutedChat> findByUserAndMutedUser(User user, User mutedUser);

}
