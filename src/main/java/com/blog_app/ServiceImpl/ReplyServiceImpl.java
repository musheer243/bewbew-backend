package com.blog_app.ServiceImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog_app.entities.Comment;
import com.blog_app.entities.Notification;
import com.blog_app.entities.Post;
import com.blog_app.entities.Reply;
import com.blog_app.entities.User;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.payloads.ReplyDto;
import com.blog_app.repositories.CommentRepo;
import com.blog_app.repositories.NotificationRepo;
import com.blog_app.repositories.PostRepo;
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
    
    @Autowired
    private PostRepo postRepo;
    
	@Override
	public ReplyDto createReply(ReplyDto replyDto, Integer userId, Integer commentId) {

        User user = this.userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "user id", userId));

        Comment comment = this.commentRepo.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "comment id", commentId));
        
        Post post = comment.getPost();

        Reply reply = this.modelMapper.map(replyDto, Reply.class);
        reply.setUser(user);
        reply.setComment(comment);
        reply.setDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        Reply savedReply = this.replyRepo.save(reply);
        
        post.setCommentCount(post.getCommentCount() + 1);
        postRepo.save(post);
        
        String content = reply.getContent();
        String extractedUsername = null;
        if (content != null && content.trim().startsWith("@")) {
            int spaceIndex = content.indexOf(" ");
            if (spaceIndex > 1) {
                extractedUsername = content.substring(1, spaceIndex).trim();
            } else {
                extractedUsername = content.substring(1).trim();
            }
        }
        System.out.println(extractedUsername);
        String baseRedirectUrl = "/api/post/view/" + post.getPostId() + "#comment-" + comment.getId() + "-reply-" + savedReply.getId();

        
     // Check if the extracted username matches the comment owner's username.
        if (extractedUsername != null && 
            extractedUsername.equalsIgnoreCase(comment.getUser().getUsername())) {
            
            // ----- Replying to a comment -----
            // Notify the comment owner if they are not the sender.
            if (!comment.getUser().equals(user)) {
                Notification notificationComment = new Notification();
                notificationComment.setSender(user);
                notificationComment.setReceiver(comment.getUser());
                notificationComment.setMessage(user.getName() + " has replied to your comment.");
                notificationComment.setPostId(post.getPostId());
                notificationComment.setCommentId(comment.getId());
                notificationComment.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                notificationComment.setRedirectUrl(baseRedirectUrl);
                notificationComment.setSenderProfilePicUrl(user.getProfilepic());
                Notification savedNotification = notificationRepo.save(notificationComment);
                notificationServiceImpl.sendNotification(savedNotification);
            }
            // Notify the post owner if they are not the sender and not the same as the comment owner.
            if (!post.getUser().equals(user) && !post.getUser().equals(comment.getUser())) {
                Notification notificationPost = new Notification();
                notificationPost.setSender(user);
                notificationPost.setReceiver(post.getUser());
                notificationPost.setMessage(user.getName() + " has replied to a comment in your post.");
                notificationPost.setPostId(post.getPostId());
                notificationPost.setCommentId(comment.getId());
                notificationPost.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                notificationPost.setRedirectUrl(baseRedirectUrl);
                notificationPost.setSenderProfilePicUrl(user.getProfilepic());
                Notification savedNotification = notificationRepo.save(notificationPost);
                notificationServiceImpl.sendNotification(savedNotification);
            }
            
        } else {
            // ----- Replying to a reply -----
            // Always notify the post owner if they are not the sender.
            if (!post.getUser().equals(user)) {
                Notification notificationPost = new Notification();
                notificationPost.setSender(user);
                notificationPost.setReceiver(post.getUser());
                notificationPost.setMessage(user.getName() + " has replied to a reply in your post.");
                notificationPost.setPostId(post.getPostId());
                notificationPost.setCommentId(comment.getId());
                notificationPost.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                notificationPost.setRedirectUrl(baseRedirectUrl);
                notificationPost.setSenderProfilePicUrl(user.getProfilepic());
                Notification savedNotification = notificationRepo.save(notificationPost);
                notificationServiceImpl.sendNotification(savedNotification);
            }
            // Notify the mentioned user in the reply content if they are not the sender.
            if (extractedUsername != null) {
                Optional<User> mentionedUserOpt = this.userRepo.findByUsername(extractedUsername);
                if (mentionedUserOpt.isPresent()) {
                    User mentionedUser = mentionedUserOpt.get();
                    if (!mentionedUser.equals(user)) {
                        Notification notificationMention = new Notification();
                        notificationMention.setSender(user);
                        notificationMention.setReceiver(mentionedUser);
                        notificationMention.setMessage(user.getName() + " mentioned you in a reply.");
                        notificationMention.setPostId(post.getPostId());
                        notificationMention.setCommentId(comment.getId());
                        notificationMention.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        notificationMention.setRedirectUrl(baseRedirectUrl);
                        notificationMention.setSenderProfilePicUrl(user.getProfilepic());
                        Notification savedNotification = notificationRepo.save(notificationMention);
                        notificationServiceImpl.sendNotification(savedNotification);
                    }
                }
            }
        }

        return this.modelMapper.map(savedReply, ReplyDto.class);
    }

	@Override
	public void deleteReply(Integer replyId) {
		Reply reply = this.replyRepo.findById(replyId)
	            .orElseThrow(() -> new ResourceNotFoundException("Reply", "reply id", replyId));
		Comment comment = reply.getComment();
		Post post = comment.getPost();
		post.setCommentCount(post.getCommentCount() - 1);
		postRepo.save(post);
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
	