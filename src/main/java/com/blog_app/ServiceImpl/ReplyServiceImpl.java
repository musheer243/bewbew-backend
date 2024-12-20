package com.blog_app.ServiceImpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog_app.entities.Comment;
import com.blog_app.entities.Notification;
import com.blog_app.entities.Reply;
import com.blog_app.entities.User;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.payloads.ReplyDto;
import com.blog_app.repositories.CommentRepo;
import com.blog_app.repositories.NotificationRepo;
import com.blog_app.repositories.ReplyRepo;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.ReplyService;
@Service
public class ReplyServiceImpl implements ReplyService {

	@Autowired
    private UserRepo userRepo;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private ReplyRepo replyRepo;

    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private NotificationServiceImpl notificationServiceImpl;
    
    @Autowired
    private NotificationRepo notificationRepo;
    
	@Override
	public ReplyDto createReply(ReplyDto replyDto, Integer userId, Integer commentId) {

        User user = this.userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "user id", userId));

        Comment comment = this.commentRepo.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "comment id", commentId));

        Reply reply = this.modelMapper.map(replyDto, Reply.class);
        reply.setUser(user);
        reply.setComment(comment);
        reply.setDate(LocalDateTime.now());

        Reply savedReply = this.replyRepo.save(reply);
        if (comment.getUser()!=user) {
			
		
     // Create a notification for the comment owner
	    Notification notification = new Notification();
	    notification.setSender(user);
	    notification.setReceiver(comment.getUser()); // The comment owner
	    notification.setMessage(user.getName() + " has replied to your comment.");
	    notification.setPostId(comment.getPost().getPostId());
	    notification.setCommentId(comment.getId());
	    notification.setTimestamp(LocalDateTime.now());

	    notificationServiceImpl.sendNotification(notification);

	    notificationRepo.save(notification);
        }
	    
	 // If the post owner is not the same as the user replying, create a notification for the post owner
	    if (!comment.getPost().getUser().equals(user)) {
	    //create A NOTIFICATION for the post owner
	    Notification notification2 = new Notification();
	    notification2.setSender(user);
	    notification2.setReceiver(comment.getPost().getUser());//the post owner
	    notification2.setMessage(user.getName() + " has replied to the comment in ur post.");
	    notification2.setPostId(comment.getPost().getPostId());
	    notification2.setCommentId(comment.getId());
	    notification2.setTimestamp(LocalDateTime.now());

	    notificationServiceImpl.sendNotification(notification2);

	    notificationRepo.save(notification2);
	    }

        return this.modelMapper.map(savedReply, ReplyDto.class);

	}

	@Override
	public void deleteReply(Integer replyId) {
		Reply reply = this.replyRepo.findById(replyId)
	            .orElseThrow(() -> new ResourceNotFoundException("Reply", "reply id", replyId));
	        this.replyRepo.delete(reply);
	}

	@Override
	public ReplyDto updateReply(ReplyDto replyDto, Integer replyId) {
		Reply reply = this.replyRepo.findById(replyId)
	            .orElseThrow(() -> new ResourceNotFoundException("Reply", "reply id", replyId));
	        reply.setContent(replyDto.getContent());
	        Reply updatedReply = this.replyRepo.save(reply);
	        return this.modelMapper.map(updatedReply, ReplyDto.class);
	}

	@Override
	public List<ReplyDto> getRepliesByComment(Integer commentId) {
		 Comment comment = this.commentRepo.findById(commentId)
		            .orElseThrow(() -> new ResourceNotFoundException("Comment", "comment id", commentId));
		        List<Reply> replies = this.replyRepo.findAllByComment(comment);
		        return replies.stream()
		            .map(reply -> this.modelMapper.map(reply, ReplyDto.class))
		            .collect(Collectors.toList());
	}
	
	
	
}
	