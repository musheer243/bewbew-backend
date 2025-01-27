package com.blog_app.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.modelmapper.internal.bytebuddy.asm.Advice.This;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog_app.entities.Notification;
import com.blog_app.entities.Post;
import com.blog_app.entities.PostLike;
import com.blog_app.entities.User;
import com.blog_app.entities.UserInteraction;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.payloads.PostDto;
import com.blog_app.payloads.PostResponse;
import com.blog_app.payloads.UserDto;
import com.blog_app.repositories.NotificationRepo;
import com.blog_app.repositories.PostLikeRepo;
import com.blog_app.repositories.PostRepo;
import com.blog_app.repositories.UserInteractionRepo;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.PostLikeService;
@Transactional
@Service
public class PostLikeServiceImpl implements PostLikeService{

//	@Autowired
//	private PostLike postLike;
	
	@Autowired
	private NotificationRepo notificationRepo;
	
	@Autowired
	private NotificationServiceImpl notificationServiceImpl;
	
	@Autowired
	private PostLikeRepo postLikeRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private UserInteractionRepo userInteractionRepo;
	
	@Transactional
	@Override
	public String toggleLikePost(int postId, int userId) {
		Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResourceNotFoundException("post", "postId", postId));
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		
		boolean hasLiked = this.postLikeRepo.existsByPostAndUser(post, user);
		
		if (hasLiked) {
			//unlike the post
			PostLike postLike = this.postLikeRepo.findByPostAndUser(post, user);
			post.getLikes().remove(postLike);
		    user.getLikes().remove(postLike);
			postLikeRepo.delete(postLike);

			//decrement the like count
			post.setLikeCount(post.getLikeCount() -1);
			postRepo.save(post);
			
			// Decrement user's total like count
	        User postOwner = post.getUser();
	        postOwner.setTotalLikes(postOwner.getTotalLikes() - 1);
	        this.userRepo.save(postOwner);
	        
	        UserInteraction interaction = this.userInteractionRepo.findByPostAndUserAndInteractionType(post, user, 1);
	        if (interaction != null && interaction.getInteractionType() == 1) {
	        	this.userInteractionRepo.delete(interaction);
			}
			
            return "Post unliked successfully!";

		}else {
			//like the post
			PostLike postLike = new PostLike();
			postLike.setPost(post);
			postLike.setUser(user);
			postLikeRepo.save(postLike);
			
			//increment the like count
			post.setLikeCount(post.getLikeCount() +1);
			postRepo.save(post);
			
			 User postOwner = post.getUser();
		        postOwner.setTotalLikes(postOwner.getTotalLikes() + 1);
		        this.userRepo.save(postOwner);
		        
		        UserInteraction interaction = new UserInteraction();
		        interaction.setUser(user);
		        interaction.setPost(post);
		        interaction.setInteractionType(1); // 1 for like
		        interaction.setInteractionScore(1); // Score for like
		        userInteractionRepo.save(interaction);	
		        
			if (post.getUser()!=user) {
			
//			// Create a notification for the post owner
		    Notification notification = new Notification();
		    notification.setSender(user);
		    notification.setReceiver(post.getUser()); // The post owner
		    notification.setMessage(user.getName() + " has liked your post.");
		    notification.setPostId(post.getPostId());
		    notification.setTimestamp(LocalDateTime.now());

		    notificationServiceImpl.sendNotification(notification);

		    notificationRepo.save(notification);
			}
            return "Post liked successfully!";


		}
	}

	@Override
	public boolean isPostLikedByUser(int postId, int userId) {
		Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResourceNotFoundException("post", "postId", postId));
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		
		return this.postLikeRepo.existsByPostAndUser(post, user);
	}
	
	@Override
	public PostResponse getLikedPostsByUser(int userId, int pageNumber, int pageSize, String sortBy, String sortDir) {
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		Sort.Direction direction = Sort.Direction.fromString(sortDir);
		Sort sort = Sort.by(direction,sortBy);
		
		org.springframework.data.domain.Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		Page<PostLike> allByUser = this.postLikeRepo.findAllByUser(user, pageable);
		
		List<PostLike> content = allByUser.getContent();
		List<PostDto> collect = content.stream().map(postLike -> modelMapper.map(postLike.getPost(), PostDto.class)).collect(Collectors.toList());
		
		PostResponse postResponse = new PostResponse();
		postResponse.setContent(collect);
		postResponse.setPageNumber(allByUser.getNumber());
		postResponse.setPageSize(allByUser.getSize());
		postResponse.setTotalElement(allByUser.getTotalElements());
		postResponse.setTotalPages(allByUser.getTotalPages());
		postResponse.setLastPage(allByUser.isLast());
		
		return postResponse;
		
	}

	@Override
	public List<UserDto> getUsersWhoLikedPost(int postId) {
		Post post = this.postRepo.findById(postId).orElseThrow(()->new ResourceNotFoundException("post","postId",postId));
		
		List<PostLike> allByPost = this.postLikeRepo.findAllByPost(post);
		
		List<UserDto> map = allByPost.stream().map(postLike -> modelMapper.map(postLike.getUser(), UserDto.class)).collect(Collectors.toList());
		return map;
	}


	

	

}
