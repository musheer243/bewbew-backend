package com.blog_app.ServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog_app.entities.Post;
import com.blog_app.entities.PostLike;
import com.blog_app.entities.SavedPost;
import com.blog_app.entities.User;
import com.blog_app.entities.UserInteraction;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.payloads.PostDto;
import com.blog_app.payloads.PostResponse;
import com.blog_app.repositories.PostRepo;
import com.blog_app.repositories.SavedPostRepo;
import com.blog_app.repositories.UserInteractionRepo;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.SavedPostService;
@Service
public class SavedPostServiceImpl implements SavedPostService {

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private SavedPostRepo savedPostRepo;

	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private UserInteractionRepo userInteractionRepo;
	
	@Transactional
	@Override
	public String toggleSavedPost(int postId, int userId) {

		Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResourceNotFoundException("post", "postId", postId));
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		
		boolean hasSaved = this.savedPostRepo.existsByPostAndUser(post, user);
		
		if (hasSaved) {
			//unlike the post
			 SavedPost savedPost = this.savedPostRepo.findByPostAndUser(post, user);
			 user.getSavedPosts().remove(savedPost);
			 post.getSavedPosts().remove(savedPost);
			savedPostRepo.delete(savedPost);
			
			//decrement the like count
			post.setSaveCount(post.getSaveCount() -1);
			postRepo.save(post);
			
			UserInteraction interaction = this.userInteractionRepo.findByPostAndUserAndInteractionType(post, user, 2);
	        if (interaction != null && interaction.getInteractionType() == 2) {
				this.userInteractionRepo.delete(interaction);
			}
			
            return "Post UnSaved successfully!";

		}else {
			//like the post
			SavedPost savedPost = new SavedPost();
			savedPost.setPost(post);
			savedPost.setUser(user);
			savedPostRepo.save(savedPost);
			
			//increment the like count
			post.setSaveCount(post.getSaveCount() +1);
			postRepo.save(post);
			
			UserInteraction interaction = new UserInteraction();
			interaction.setUser(user);
	        interaction.setPost(post);
	        interaction.setInteractionType(2); // 2 for save
	        interaction.setInteractionScore(2); // Score for save
	        userInteractionRepo.save(interaction);
			
            return "Post Saved successfully!";

		}
	}
	
	@Override
	public boolean isPostSavedByUser(int postId, int userId) {
		Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResourceNotFoundException("post", "postId", postId));
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		
		return this.savedPostRepo.existsByPostAndUser(post, user);
	}


	@Override
	public PostResponse getSavedPostsByUser(int userId, int pageNumber, int pageSize, String sortBy, String sortDir) {
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		Sort.Direction direction = Sort.Direction.fromString(sortDir);
		Sort sort = Sort.by(direction,sortBy);
	
		org.springframework.data.domain.Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		Page<SavedPost> allByUser = this.savedPostRepo.findAllByUser(user, pageable);
		List<PostDto> collect = allByUser.stream().map(postLike-> modelMapper.map(postLike.getPost(), PostDto.class)).collect(Collectors.toList());
		
		PostResponse postResponse = new PostResponse();
		
		postResponse.setContent(collect);
		postResponse.setPageNumber(allByUser.getNumber());
		postResponse.setPageSize(allByUser.getSize());
		postResponse.setTotalElement(allByUser.getTotalElements());
		postResponse.setTotalPages(allByUser.getTotalPages());
		postResponse.setLastPage(allByUser.isLast());
		
		return postResponse;
	}

	
	

	}
	
	

