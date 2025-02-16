package com.blog_app.ServiceImpl;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.blog_app.entities.Category;
import com.blog_app.entities.NotInterestedPost;
import com.blog_app.entities.Notification;
import com.blog_app.entities.Post;
import com.blog_app.entities.PostLike;
import com.blog_app.entities.User;
import com.blog_app.exceptions.PostUpdateDataNotFoundException;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.exceptions.SecurityResourceNotFoundException;
import com.blog_app.payloads.PostDto;
import com.blog_app.payloads.PostResponse;
import com.blog_app.repositories.CategoryRepo;
import com.blog_app.repositories.NotInterestedPostRepo;
import com.blog_app.repositories.NotificationRepo;
import com.blog_app.repositories.PostRepo;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.BadgeService;
import com.blog_app.services.FIleServiceMedia;
import com.blog_app.services.MonthlyLeaderboardService;
import com.blog_app.services.PostService;
import com.blog_app.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;



@Service
public class PostServiceImpl implements PostService {

	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private NotInterestedPostRepo notInterestedPostRepo;
	
	@Autowired
	private NotificationRepo notificationRepo;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private FIleServiceMedia fIleServiceMedia;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private CategoryRepo categoryRepo;
	
//	@Autowired
//	private FileService fileService;
//	
	@Autowired
	private ObjectMapper objectMapper;
	
//	@Value("${project.image}")
//	private String path;
	
	@Autowired
	private BadgeService badgeService;
	
	@Autowired
	private MonthlyLeaderboardService monthlyLeaderboardService;
	
	@Autowired
	private NotificationServiceImpl notificationServiceImpl;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void deletePost(Integer postId) {
	
		// Find the post or throw exception if not found
	    Post post = this.postRepo.findById(postId)
	            .orElseThrow(() -> new ResourceNotFoundException("Post", "post id", postId));

	    // Retrieve the list of media file names associated with the post
	    List<String> mediaFileNames = post.getMediaFileNames();

	    // Pass the media file names to the file service for handling deletion
	    this.fIleServiceMedia.deletePostFiles(mediaFileNames);

	    // Finally, delete the post from the repository and decrease user total posts
	    User user = post.getUser();
	    user.setTotalPosts(user.getTotalPosts()-1);
	    userRepo.save(user);
	    
	    this.postRepo.delete(post);
	    
	}
	
	

	@Override
	public PostResponse getAllPost(int pageNumber, int pageSize,String sortBy, String sortDir) {
		
		Sort.Direction sortDirection = Sort.Direction.fromString(sortDir);
		Sort sort = Sort.by(sortDirection, sortBy);
		
		
		org.springframework.data.domain.Pageable p = PageRequest.of(pageNumber, pageSize, sort);

		org.springframework.data.domain.Page<Post> pagePost= this.postRepo.findAllByIsPublishedTrueAndCloseFriendsOnlyFalseAndUserIsPrivateFalse(p);
		
		
		List<Post> allPosts = pagePost.getContent();
		List<PostDto> postDtos = allPosts.stream().map((post)-> this.modelMapper.map(post, PostDto.class)).collect(Collectors.toList()); 
	
		PostResponse postResponse = new PostResponse();
		
		postResponse.setContent(postDtos);
		postResponse.setPageNumber(pagePost.getNumber());
		postResponse.setPageSize(pagePost.getSize());
		postResponse.setTotalElement(pagePost.getTotalElements());
		postResponse.setTotalPages(pagePost.getTotalPages());
		postResponse.setLastPage(pagePost.isLast());
		
		return postResponse;
	 }

	@Override
	public PostDto getPostById(Integer postId) {
	Post pot = this.postRepo.findById(postId).orElseThrow(()-> new ResourceNotFoundException ("Post","id",postId));
		
	//ensuring the post is published
	if (!pot.isPublished()) {
		throw new ResourceNotFoundException("post", "id", postId);
	}
	return this.modelMapper.map(pot, PostDto.class);

	}

	@Override
	public PostResponse getPostByCategory(Integer categoryId, int pageNumber, int pageSize, String sortBy, String sortDir) {
		
		Category cat = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException
				("Category", "category id", categoryId));
		
		Sort.Direction direction= Sort.Direction.fromString(sortDir);
		Sort sort = Sort.by(direction, sortBy);
		
		org.springframework.data.domain.Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		org.springframework.data.domain.Page<Post> pagePost = this.postRepo.findByCategoryAndIsPublishedTrueAndCloseFriendsOnlyFalseAndUserIsPrivateFalse(cat, pageable);
		
	   List<Post> posts= pagePost.getContent();
	    
	   List<PostDto>postDtos = posts.stream().map((post) -> this.modelMapper.map(post, PostDto.class)).collect(Collectors.toList());
	   
	   PostResponse postResponse = new PostResponse(); 
	   
	    postResponse.setContent(postDtos);
		postResponse.setPageNumber(pagePost.getNumber());
		postResponse.setPageSize(pagePost.getSize());
		postResponse.setTotalElement(pagePost.getTotalElements());
		postResponse.setTotalPages(pagePost.getTotalPages());
		postResponse.setLastPage(pagePost.isLast());
	 	return postResponse;
		
	}

	@Override
	public PostResponse getPostByUser(Integer userId, int pageNumber, int pageSize, String sortBy, String sortDir) {

	User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException
			("User", "userId", userId));
	
	 // Get the current logged-in user
    User loggedInUser = userService.getLoggedInUser();
    Integer viewerId = loggedInUser.getId();
	
	Sort.Direction direction = Sort.Direction.fromString(sortDir);
	Sort sort = Sort.by(direction,sortBy);
	
	org.springframework.data.domain.Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
	org.springframework.data.domain.Page<Post> pagePost = this.postRepo.findByUserAndIsPublishedTrue(user, pageable);
	
	List<PostDto> posts = pagePost.getContent().stream()
		    .filter(post -> 
		        // Allow the user to see all their posts
		        userId.equals(viewerId) || 
		        // If not the user, check if the post is not for close friends only or if the viewer is a close friend
		        !post.isCloseFriendsOnly() || 
		        user.getCloseFriends().stream().anyMatch(closeFriend -> closeFriend.getId()==viewerId)
		    )
		    .map(post -> this.modelMapper.map(post, PostDto.class))
		    .collect(Collectors.toList());
	
	PostResponse postResponse = new PostResponse();
	postResponse.setContent(posts);
	postResponse.setPageNumber(pagePost.getNumber());
	postResponse.setPageSize(pagePost.getSize());
	postResponse.setTotalElement(pagePost.getTotalElements());
	postResponse.setTotalPages(pagePost.getTotalPages());
	postResponse.setLastPage(pagePost.isLast());
	
	return postResponse;
	
	}

	@Override
	public List<PostDto> searchPosts(String keyword) {

		List<Post> posts = this.postRepo.findByTitleContainingAndIsPublishedTrueAndCloseFriendsOnlyFalseAndUserIsPrivateFalse(keyword);
		List<PostDto> postDtos = posts.stream()
                .map(post -> this.modelMapper.map(post, PostDto.class))
                .collect(Collectors.toList());			
		return postDtos;
	}



	
	
	@Override
	public PostDto createPostWithMedia(String postDto, Integer userId, Integer categoryId, List<MultipartFile> files) throws IOException {
	    // Fetch User and Category entities
	    User user = this.userRepo.findById(userId)
	            .orElseThrow(() -> new ResourceNotFoundException("User", "User id", userId));

	    Category category = this.categoryRepo.findById(categoryId)
	            .orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));

	    // Deserialize the JSON postDto string into PostDto object
	    PostDto value = objectMapper.readValue(postDto, PostDto.class);

	    Post post = this.modelMapper.map(value, Post.class);

	    // Set added date, user, and category
	 // Check if the user provided a scheduled date for the post
	    if (value.getScheduledDate() != null) {
	        post.setAddedDate(value.getScheduledDate()); // Set scheduled date
	        post.setPublished(false); // Set to unpublished if scheduled
	    } else {
	        post.setAddedDate(LocalDateTime.now()); // Set the current date if not scheduled
	        post.setPublished(true); // Immediately publish if no scheduled date
	    }	
	    post.setUser(user);
	    post.setCategory(category);

	    // Handle media files (images/videos) if provided
	    if (files != null && !files.isEmpty()) {
	        List<String> uploadedFiles = this.fIleServiceMedia.uploadMedia(files);
	        post.setMediaFileNames(uploadedFiles);  // Set uploaded media files
	    }
	    else {
			throw new PostUpdateDataNotFoundException("image or video is necessary to create post as it attracts other user to read it");
		}
	    
	    if (value.isCloseFriendsOnly()) {
	        post.setCloseFriendsOnly(value.isCloseFriendsOnly());
	    } else {
	        post.setCloseFriendsOnly(false); // default to false if not provided
	    }
	    
	    // Save the post
	    Post newPost = this.postRepo.save(post);
	    
	 // If the post is published immediately, update user's total posts, badge, and leaderboard
	    if (post.isPublished()) {
	        // Increment user's total post count
	        user.setTotalPosts(user.getTotalPosts() + 1);

	        // Update user's badge
	        String newBadge = this.badgeService.getBadgeForPostCount(user.getTotalPosts());
	        user.setBadge(newBadge);
	        
	        // Save the updated user
	        this.userRepo.save(user);
	        
	        if (user.isPrivate()==false) {
	        	 // Update Monthly Leaderboard
		        this.monthlyLeaderboardService.updateMonthlyLeaderboard(user.getName());
		        
		        if (!post.isCloseFriendsOnly()) {
		        	for(User user1: user.getFollowers()) {
		        	    Notification notification = new Notification();
		        	    notification.setSender(user);
		        	    notification.setReceiver(user1);
		        	    notification.setMessage(user1.getName() + " has just uploaded a post");
		        	    notification.setTimestamp(LocalDateTime.now());
		        	    notification.setPostId(newPost.getPostId());
		        	    
		        	    notificationServiceImpl.sendNotification(notification);
		        	    notificationRepo.save(notification);
		        	    }
				}
		        
			} 
	       
	    }
	    
	   
	    // Return saved post as PostDto
	    return this.modelMapper.map(newPost, PostDto.class);
	}
	
	



	@Override
	public PostDto updatePostWithMedia(Integer postId, String postDtoJson, List<MultipartFile> newFiles) throws IOException {
	    Post post = postRepo.findById(postId)
	        .orElseThrow(() -> new ResourceNotFoundException("post", "postId", postId));

	    PostDto dto = objectMapper.readValue(postDtoJson, PostDto.class);

	    // Update basics
	    if (dto.getTitle() != null) post.setTitle(dto.getTitle());
	    if (dto.getContent() != null) post.setContent(dto.getContent());
	    post.setCloseFriendsOnly(dto.isCloseFriendsOnly());

	    // Suppose we store which old links the user wants to keep
	    List<String> keptOldLinks = dto.getKeptOldLinks(); // new field in PostDto
	    if (keptOldLinks == null) {
	        keptOldLinks = new ArrayList<>();
	    }
	    
	 // Create a final copy for the lambda
	    final List<String> finalKeptOldLinks = keptOldLinks;

	    // existing list from DB
	    List<String> current = post.getMediaFileNames();
	    if (current == null) {
	        current = new ArrayList<>();
	    }
	    
	 // 5) Find which old links the user is removing
	    //    (i.e., in currentMedia but NOT in keptOldLinks)
	    List<String> removedLinks = current.stream()
	        .filter(oldLink -> !finalKeptOldLinks.contains(oldLink))
	        .toList();

	    // remove any old link not in keptOldLinks
	    // optional: physically remove from S3 if you want
	    List<String> finalList = current.stream()
	        .filter(keptOldLinks::contains)
	        .collect(Collectors.toList());
	    
	    //    (so you don't keep unused media in your storage)
	    if (!removedLinks.isEmpty()) {
	        this.fIleServiceMedia.deletePostFiles(removedLinks);
	    }

	    // now handle new files
	    if (newFiles != null && !newFiles.isEmpty()) {
	        List<String> uploaded = fIleServiceMedia.uploadMedia(newFiles);
	        finalList.addAll(uploaded);
	    }

	    post.setMediaFileNames(finalList);

	    Post saved = postRepo.save(post);
	    return modelMapper.map(saved, PostDto.class);
	}



	@Override
	public PostResponse getPostsByCategoryTitle(String categoryTitle, int pageNumber, int pageSize, String sortBy,
			String sortDir) {

		List<Category> categories = this.categoryRepo.findByCategoryTitleContaining(categoryTitle);
		
		 // Check if any categories are found
	    if (categories.isEmpty()) {
	        // Handle the case when no category matches
	        throw new SecurityResourceNotFoundException("Category", "title", categoryTitle);
	    }

	    //Category category = categories.get(0);  // Get the first matching category
	    
		Sort.Direction direction= Sort.Direction.fromString(sortDir);
		Sort sort = Sort.by(direction, sortBy);
		
		org.springframework.data.domain.Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		org.springframework.data.domain.Page<Post> pagePost = this.postRepo.findByCategoryInAndIsPublishedTrueAndCloseFriendsOnlyFalseAndUserIsPrivateFalse(categories, pageable);

		 List<Post> posts= pagePost.getContent();
		    
		   List<PostDto>postDtos = posts.stream().map((post) -> this.modelMapper.map(post, PostDto.class)).collect(Collectors.toList());
		   
		   PostResponse postResponse = new PostResponse(); 
		   
		    postResponse.setContent(postDtos);
			postResponse.setPageNumber(pagePost.getNumber());
			postResponse.setPageSize(pagePost.getSize());
			postResponse.setTotalElement(pagePost.getTotalElements());
			postResponse.setTotalPages(pagePost.getTotalPages());
			postResponse.setLastPage(pagePost.isLast());
		 	return postResponse;
		
		
	}



	@Override
	public List<PostDto> getScheduledPosts() {
		List<Post> scheduledPosts = postRepo.findAllByIsPublishedFalse();
	    return scheduledPosts.stream()
	                         .map(post -> modelMapper.map(post, PostDto.class))
	                         .collect(Collectors.toList());		
	}
	
	
	@Override
	public List<PostDto> getScheduledPostsForUser(Integer userId) {
	    // Fetch the user entity by userId
	    User user = userRepo.findById(userId)
	            .orElseThrow(() -> new ResourceNotFoundException("User", "User id", userId));

	    // Fetch all unpublished posts for the user
	    List<Post> scheduledPosts = postRepo.findAllByUserAndIsPublishedFalse(user);

	    // Convert the list of Post entities to PostDto
	    return scheduledPosts.stream()
	                         .map(post -> modelMapper.map(post, PostDto.class))
	                         .collect(Collectors.toList());
	}



	@Override
	public PostResponse getPostsByUsersFollowing(Integer userId, int pageNumber, int pageSize, String sortBy,
			String sortDir) {
		
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		
		Set<User> following = user.getFollowing();
		
		if (following==null & following.isEmpty()) {
			throw new PostUpdateDataNotFoundException("follow users to see their posts");
		}
		
		 // Fetch posts that the user marked as "Not Interested"
	    Set<Post> notInterestedPosts = this.notInterestedPostRepo.findPostByUser(user);
	    		
	    // Get the list of posts that the user has already liked
		Set<Post> likedPosts = user.getLikes().stream().map(PostLike::getPost).collect(Collectors.toSet());
		
		List<User> collect = following.stream().map(myFollowing-> modelMapper.map(myFollowing, User.class)).collect(Collectors.toList());
		
		Sort.Direction direction= Sort.Direction.fromString(sortDir);
		Sort sort = Sort.by(direction, sortBy);
		
		org.springframework.data.domain.Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		Page<Post> byUserInAndIsPublishedTrue = this.postRepo.findByUserInAndIsPublishedTrue(collect, pageable);
			
		 List<PostDto> collect2 = byUserInAndIsPublishedTrue.getContent().stream()
				 .filter(post -> !likedPosts.contains(post))
				 .filter(post -> !notInterestedPosts.contains(post))  // Exclude "not interested" posts
				 .filter(post-> !post.isCloseFriendsOnly() || post.getUser().getCloseFriends().contains(user))
				 .map(posts-> modelMapper.map(posts, PostDto.class)).collect(Collectors.toList());
		
		//List<PostDto> collect2 = content.stream().map(posts-> modelMapper.map(posts, PostDto.class)).collect(Collectors.toList());
		

		    
		PostResponse postResponse =  new PostResponse();
		postResponse.setContent(collect2);
		postResponse.setPageNumber(byUserInAndIsPublishedTrue.getNumber());
		postResponse.setPageSize(byUserInAndIsPublishedTrue.getSize());
		postResponse.setTotalElement(byUserInAndIsPublishedTrue.getTotalElements());
		postResponse.setTotalPages(byUserInAndIsPublishedTrue.getTotalPages());
		postResponse.setLastPage(byUserInAndIsPublishedTrue.isLast());
		return postResponse;
	}




	

}
