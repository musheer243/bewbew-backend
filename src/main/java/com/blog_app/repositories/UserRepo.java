package com.blog_app.repositories;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.blog_app.entities.User;

public interface UserRepo extends JpaRepository<User, Integer> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);
	
	boolean existsByUsername(String username);
	
	Optional<User> findByUsername(String username);
	
	List<User> findByNameContainingOrUsernameContaining(String name, String username);
	
	Optional<User> findByUsernameOrEmail(String username, String email);
}