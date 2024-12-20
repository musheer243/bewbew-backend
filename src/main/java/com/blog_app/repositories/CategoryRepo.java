package com.blog_app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.Category;

public interface CategoryRepo extends JpaRepository<Category, Integer>{

	List<Category> findByCategoryTitleContaining(String title);
	
}
