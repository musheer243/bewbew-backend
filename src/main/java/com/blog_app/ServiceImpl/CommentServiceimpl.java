package com.blog_app.ServiceImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.modelmapper.ModelMapper;
import org.modelmapper.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.blog_app.entities.Comment;
import com.blog_app.entities.Notification;
import com.blog_app.entities.Post;
import com.blog_app.entities.User;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.payloads.CommentDto;
import com.blog_app.repositories.CommentRepo;
import com.blog_app.repositories.NotificationRepo;
import com.blog_app.repositories.PostRepo;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.CommentService;

@Service
public class CommentServiceimpl implements CommentService {

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private NotificationServiceImpl notificationServiceImpl;
	
	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private CommentRepo commentRepo;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private NotificationRepo notificationRepo;
	
	@Override
	public CommentDto createComment(CommentDto commentDto, Integer userId, Integer postId) {

		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User", "user id", userId));

		Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post", "post id", postId));
		
		Comment comment = this.modelMapper.map(commentDto, Comment.class);
		comment.setUser(user);
		comment.setPost(post);
	    comment.setDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		
		post.setCommentCount(post.getCommentCount() +1);
		this.postRepo.save(post);

		Comment saveComment = this.commentRepo.save(comment);
		
		if (post.getUser()!=user) {
			
		
		// Create a notification for the post owner
	    Notification notification = new Notification();
	    notification.setSender(user);
	    notification.setReceiver(post.getUser()); // The post owner
	    notification.setMessage(user.getName() + " has commented on your post.");
	    notification.setPostId(post.getPostId());
	    notification.setCommentId(saveComment.getId());
        notification.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        notification.setSenderProfilePicUrl(user.getProfilepic());
        notification.setRedirectUrl("/api/post/view/" + post.getPostId() + "#comment-" + saveComment.getId());

	    Notification save = notificationRepo.save(notification);

	    notificationServiceImpl.sendNotification(save);
	    


		}
		
		return this.modelMapper.map(saveComment, CommentDto.class);
	}

	@Override
	public void deleteComment(Integer commentId) {
    
		Comment comment= this.commentRepo.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "commentid", commentId));
	 
		Post post = comment.getPost();
		post.setCommentCount(post.getCommentCount()-1);
	    this.postRepo.save(post);
		this.commentRepo.delete(comment);
	
	}

	@Override
	public CommentDto updateComment(CommentDto commentDto, Integer commentId) {
		Comment comment = this.commentRepo.findById(commentId).orElseThrow(()-> new ResourceNotFoundException("comment", "commentId", commentId));
		comment.setContent(commentDto.getContent());
		Comment save = this.commentRepo.save(comment);
		return this.modelMapper.map(save, CommentDto.class);
		
	}

	@Override
	public Page<CommentDto> getCommentsByPost(Integer postId, int page, int size) {
	    Post post = this.postRepo.findById(postId)
	            .orElseThrow(() -> new ResourceNotFoundException("post", "postId", postId));
	    
	    Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
	    Page<Comment> commentPage = commentRepo.findAllByPost(post, pageable);
	    
	    // Convert each Comment entity to CommentDto
	    return commentPage.map(comment -> modelMapper.map(comment, CommentDto.class));
	}


}
