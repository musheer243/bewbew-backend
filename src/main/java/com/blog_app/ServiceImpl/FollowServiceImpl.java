package com.blog_app.ServiceImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog_app.entities.FollowRequest;
import com.blog_app.entities.Notification;
import com.blog_app.entities.User;
import com.blog_app.exceptions.PostUpdateDataNotFoundException;
import com.blog_app.exceptions.ResourceNotFoundException;
import com.blog_app.repositories.FollowRequestRepo;
import com.blog_app.repositories.NotificationRepo;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.FollowService;

@Service
public class FollowServiceImpl implements FollowService {

	@Autowired
	private FollowRequestRepo followRequestRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private NotificationServiceImpl notificationServiceImpl;
	
	@Autowired
	private NotificationRepo notificationRepo;
	
	@Override
	public FollowRequest sendFollowRequest(int senderId, int receiverId) {

		User sender = this.userRepo.findById(senderId).orElseThrow(()-> new ResourceNotFoundException ("User","id",senderId));
		
	    User receiver = this.userRepo.findById(receiverId).orElseThrow(()-> new ResourceNotFoundException("User", "id", receiverId));
	    
	    if (!receiver.isPrivate()) {
	    	sender.getFollowing().add(receiver);
            receiver.getFollowers().add(sender);
            
            sender.setTotalFollowings(sender.getTotalFollowings() +1);
            receiver.setTotalFollowers(receiver.getTotalFollowers() +1);
            
            userRepo.save(sender);
            userRepo.save(receiver);
            
            Notification existingNotification = notificationRepo.findBySenderAndReceiverAndRedirectUrl(sender, receiver, "/profile/" + sender.getId());

            if (existingNotification == null) {

            Notification notification = new Notification();
            notification.setSender(sender);
            notification.setReceiver(receiver);
            notification.setMessage(sender.getName() + " started following you");
            notification.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            notification.setRedirectUrl("/profile/" + sender.getId()); // Example: /profile/123
            notification.setSenderProfilePicUrl(sender.getProfilepic());
            Notification save = notificationRepo.save(notification);
            notificationServiceImpl.sendNotification(save);

            }
            return null;
            
            
		}
	    
	 // 1) Check if a PENDING request from this sender -> receiver already exists
	    FollowRequest existingFR = followRequestRepo
	        .findBySenderIdAndReceiverIdAndStatus(senderId, receiverId, "Pending");

	    if (existingFR != null) {
	        // Already have a pending request, so no need to insert duplicates.
	        return existingFR;
	    }
	    
	    FollowRequest followRequest = new FollowRequest();
	    
        followRequest.setSender(sender);
        followRequest.setReceiver(receiver);
        followRequest.setSentAt(LocalDateTime.now());
        followRequest.setStatus("Pending");
        
        Notification existingNotification = notificationRepo.findBySenderAndReceiverAndRedirectUrl(sender, receiver, "/profile/" + sender.getId());

        if (existingNotification == null) {
        Notification notification = new Notification();
        
        notification.setSender(sender);
        notification.setReceiver(receiver);
        notification.setMessage(sender.getName() + " has requested to follow you");
        notification.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        notification.setRedirectUrl("/profile/" + sender.getId()); // Example: /profile/123
        notification.setSenderProfilePicUrl(sender.getProfilepic()); // Assuming you have a method like getProfilePictureUrl() in your User entity

        Notification save = notificationRepo.save(notification);
        notificationServiceImpl.sendNotification(save);

        }
        return followRequestRepo.save(followRequest);
    }

	@Override
	public void acceptFollowRequest(int requestId) {

		FollowRequest followRequest = followRequestRepo.findById(requestId).orElseThrow(()-> new ResourceNotFoundException("FollowRequest", "id", requestId));
	
		followRequest.setStatus("Accepted");
        User sender = followRequest.getSender();
        User receiver = followRequest.getReceiver();

        sender.getFollowing().add(receiver);
        receiver.getFollowers().add(sender);
        
        sender.setTotalFollowings(sender.getTotalFollowings() +1);
        receiver.setTotalFollowers(receiver.getTotalFollowers() +1);      

        userRepo.save(sender);
        userRepo.save(receiver);
        followRequestRepo.save(followRequest);
        
     // Check for duplicate notification before sending
        Notification existingNotification = notificationRepo.findBySenderAndReceiverAndRedirectUrl(receiver, sender, "/profile/" + receiver.getId());

        if (existingNotification == null) {
        
        Notification notification = new Notification();
        notification.setSender(receiver);
        notification.setReceiver(sender);
        notification.setMessage(receiver.getName() + " has accepted your follow request");
        notification.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        notification.setRedirectUrl("/profile/" + receiver.getId()); // Example: /profile/123
        notification.setSenderProfilePicUrl(receiver.getProfilepic()); // Assuming you have a method like getProfilePictureUrl() in your User entity

        notificationServiceImpl.sendNotification(notification);
        notificationRepo.save(notification);     
        }
	}

	@Override
	public void declineFollowRequest(int requestId) {

		 FollowRequest followRequest = followRequestRepo.findById(requestId).orElseThrow(() -> new ResourceNotFoundException("FollowRequest", "id", requestId));

	        followRequest.setStatus("Rejected");
	        followRequestRepo.save(followRequest);
	        
	     // Check for duplicate notification before sending
	        Notification existingNotification = notificationRepo.findBySenderAndReceiverAndRedirectUrl(followRequest.getReceiver(), followRequest.getSender(), "/profile/" + followRequest.getReceiver().getId());

	        if (existingNotification == null) {
	        
	        Notification notification = new Notification();
	        
	        notification.setSender(followRequest.getReceiver());
	        notification.setReceiver(followRequest.getSender());
	        notification.setMessage(followRequest.getReceiver().getName() + " has declined your follow request");
	        notification.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
	        notification.setSenderProfilePicUrl(followRequest.getReceiver().getProfilepic()); // Assuming you have a method like getProfilePictureUrl() in your User entity
	        notification.setRedirectUrl("/profile/" + followRequest.getReceiver().getId());
	        notification.setSenderProfilePicUrl(followRequest.getReceiver().getProfilepic());
	        notificationServiceImpl.sendNotification(notification);
	        notificationRepo.save(notification);
	        }
	        
	}

	@Override
	public void removeFollower(int userId, int followerId) {
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException ("User","id",userId));
		
	    User follower = this.userRepo.findById(followerId).orElseThrow(()-> new ResourceNotFoundException("User", "id", followerId));
	    
	    if (user.getFollowers().contains(follower)) {
	    	user.getFollowers().remove(follower);
	    	follower.getFollowing().remove(user);
	    	
	    	  // Decrement followers and followings count
	        user.setTotalFollowers(user.getTotalFollowers() - 1);
	        follower.setTotalFollowings(follower.getTotalFollowings() - 1);

	    	userRepo.save(user);
	    	userRepo.save(follower);
			
		}else {
			throw new PostUpdateDataNotFoundException("This user is not in your followers list");
		}
	    	}

	@Override
	public void unfollowUser(int userId, int followingId) {
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException ("User","id",userId));
		
	    User following = this.userRepo.findById(followingId).orElseThrow(()-> new ResourceNotFoundException("User", "id", followingId));
	    
	    if (user.getFollowing().contains(following)) {
	    	user.getFollowing().remove(following);
	    	following.getFollowers().remove(user);
	    	
	    	// Decrement followers and followings count
	        user.setTotalFollowings(user.getTotalFollowings() - 1);
	        following.setTotalFollowers(following.getTotalFollowers() - 1);
	    	
	    	userRepo.save(user);
	    	userRepo.save(following);
		}else {
			throw new PostUpdateDataNotFoundException("You are not following this user");
		}
		
	}

	@Override
	public List<FollowRequest> getAllReceivedRequests(int receiverId) {
		// TODO Auto-generated method stub
	    return followRequestRepo.findAllByReceiverIdAndStatus(receiverId, "Pending");
	}
	
	
}
