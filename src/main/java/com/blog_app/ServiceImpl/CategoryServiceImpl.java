package com.blog_app.ServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.internal.bytebuddy.asm.Advice.This;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog_app.entities.Category;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.payloads.CategoryDto;
import com.blog_app.repositories.CategoryRepo;
import com.blog_app.services.CategoryService;
@Service
public class CategoryServiceImpl implements CategoryService{

	@Autowired
	private CategoryRepo categoryRepo;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Override
	public CategoryDto createCategory(CategoryDto categoryDto) {
		
		Category cat = this.modelMapper.map(categoryDto, Category.class);
		Category addedCat = this.categoryRepo.save(cat);
		
		return this.modelMapper.map(addedCat, CategoryDto.class);
	}

	@Override
	public CategoryDto updateCategory(CategoryDto categoryDto, Integer categoryId) {
		
		Category cat = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category", "category id", categoryId));
		
		cat.setCategoryTitle(categoryDto.getCategoryTitle());
		cat.setCategoryDescription(categoryDto.getCategoryDescription());
	
		Category updatecat = this.categoryRepo.save(cat);
		
		return this.modelMapper.map(updatecat, CategoryDto.class);
	}

	@Override
	public void deleteCategory(Integer categoryId) {
		
		Category cat = this.categoryRepo.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("Category",  "category id", categoryId));
		this.categoryRepo.delete(cat);	
	}

	@Override
	public CategoryDto getCategory(Integer categoryId) {
		
		Category cat = this.categoryRepo.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("Category",  "category id", categoryId));
		
		return this.modelMapper.map(cat, CategoryDto.class);
	}

	@Override
	public List<CategoryDto> getCategories() {
		
		
		List<Category> categories = this.categoryRepo.findAll();
		List<CategoryDto> catDots =(List<CategoryDto>) categories.stream().map((cat)-> this.modelMapper.map(cat, CategoryDto.class)).collect(Collectors.toList()); 
	
		return catDots;
	}

	@Override
	public List<CategoryDto> searchCategory(String Keyword) {
		List<Category> byCategoryTitleContaining = this.categoryRepo.findByCategoryTitleContaining(Keyword);
		List<CategoryDto> collect = byCategoryTitleContaining.stream().map(category -> modelMapper.map(category, CategoryDto.class)).collect(Collectors.toList());
		return collect;
		
	}
	

}
