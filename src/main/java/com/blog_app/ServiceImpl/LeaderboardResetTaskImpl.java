package com.blog_app.ServiceImpl;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.blog_app.repositories.MonthlyLeaderboardRepo;
import com.blog_app.services.LeaderboardResetTask;
@Service
public class LeaderboardResetTaskImpl implements LeaderboardResetTask {

	@Autowired
	private MonthlyLeaderboardRepo monthlyLeaderboardRepo;
	
	@Override
	@Scheduled(cron = "0 0 0 1 * ?") // Runs at 12:00 AM on the 1st of every month
	public void resetLeaderboard() {
		
		LocalDate threeMonthAgo = LocalDate.now(ZoneId.of("Asia/Kolkata")).minusMonths(3).withDayOfMonth(1);
		
		//deleting data before 3 months
		monthlyLeaderboardRepo.deleteByMonthBefore(threeMonthAgo);
	}

}
