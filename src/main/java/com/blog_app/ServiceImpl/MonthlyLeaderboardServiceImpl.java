package com.blog_app.ServiceImpl;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog_app.entities.MonthlyLeaderboard;
import com.blog_app.repositories.MonthlyLeaderboardRepo;
import com.blog_app.services.MonthlyLeaderboardService;
@Service

public class MonthlyLeaderboardServiceImpl implements MonthlyLeaderboardService {

	@Autowired
	private MonthlyLeaderboardRepo monthlyLeaderboardRepo;
	
	
	@Override
	public void updateMonthlyLeaderboard(String username, String profilePic) {
		
		// Get the first day of the current month
        LocalDate currentMonth = LocalDate.now(ZoneId.of("Asia/Kolkata")).withDayOfMonth(1);

        // Check if user already has an entry for the current month
        MonthlyLeaderboard leaderboard = monthlyLeaderboardRepo.findByUsernameAndMonth(username, currentMonth);

        if(leaderboard==null) {
            // If no entry exists, create a new one
        	leaderboard = new MonthlyLeaderboard();
        	leaderboard.setUsername(username);
        	leaderboard.setPostCount(1);
        	leaderboard.setMonth(currentMonth);
        	leaderboard.setProfilePic(profilePic);
        } else {
            // If an entry exists, just increment the post count
            leaderboard.setPostCount(leaderboard.getPostCount() + 1);
        }
        
        monthlyLeaderboardRepo.save(leaderboard);
        }
	}


