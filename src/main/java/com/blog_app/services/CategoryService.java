package com.blog_app.services;

import java.util.List;

import org.springframework.core.annotation.MergedAnnotations.Search;

import com.blog_app.payloads.CategoryDto;

public interface CategoryService {

	//Create
	 CategoryDto createCategory(CategoryDto categoryDto);
	
	//Update
	CategoryDto updateCategory(CategoryDto categoryDto, Integer categoryId);
	
	//delete
	void deleteCategory(Integer categoryId);
	
	//get
	CategoryDto getCategory(Integer categoryId);
	
	//get all

	List<CategoryDto> getCategories();
	
	//search category 
	List<CategoryDto> searchCategory (String Keyword);
}
