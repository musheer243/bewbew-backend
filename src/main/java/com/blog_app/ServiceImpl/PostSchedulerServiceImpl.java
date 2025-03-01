package com.blog_app.ServiceImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.blog_app.entities.Post;
import com.blog_app.entities.User;
import com.blog_app.repositories.PostRepo;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.BadgeService;
import com.blog_app.services.MonthlyLeaderboardService;
import com.blog_app.services.PostSchedulerService;
@Service
public class PostSchedulerServiceImpl implements PostSchedulerService {

	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private BadgeService badgeService;
	
	@Autowired
	private MonthlyLeaderboardService monthlyLeaderboardService;
	
	@Autowired
	private UserRepo userRepo;

	@Override
	@Scheduled(fixedRate = 60000) // Check every minute
	public void publishScheduledPosts() {
	    // Use India/Kolkata zone for "now"
	    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
	    System.out.println("Checking for scheduled posts at: " + now);

	    // Fetch all posts where `isPublished` is false and `addedDate` is before our India-based 'now'
	    List<Post> scheduledPosts = postRepo.findAllByIsPublishedFalseAndAddedDateBefore(now);

	    if (scheduledPosts.isEmpty()) {
	        System.out.println("No scheduled posts to publish at: " + now);
	    } else {
	        System.out.println("Found scheduled posts to publish: " + scheduledPosts.size());
	    }

	    // Publish each post
	    for (Post post : scheduledPosts) {
	        post.setPublished(true);
	        postRepo.save(post); // Save the updated post
	        System.out.println("Published post ID: " + post.getPostId());

	        // Increment user's total post count, update badge, and leaderboard
	        User user = post.getUser();
	        user.setTotalPosts(user.getTotalPosts() + 1);

	        // Update user's badge
	        String newBadge = badgeService.getBadgeForPostCount(user.getTotalPosts());
	        user.setBadge(newBadge);

	        // Save updated user
	        userRepo.save(user);

	        // Update Monthly Leaderboard if user is not private
	        if (!user.isPrivate()) {
	            monthlyLeaderboardService.updateMonthlyLeaderboard(user.getUsername(), user.getProfilepic());
	        }
	    }
	}

}
