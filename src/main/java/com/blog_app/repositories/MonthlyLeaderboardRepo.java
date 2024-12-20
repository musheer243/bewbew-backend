package com.blog_app.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog_app.entities.MonthlyLeaderboard;

public interface MonthlyLeaderboardRepo extends JpaRepository<MonthlyLeaderboard, Integer>{

    // Find a user's leaderboard entry for the current month
	MonthlyLeaderboard findByUsernameAndMonth(String username,LocalDate month);
	
	//top 100 users for leaderboard
    List<MonthlyLeaderboard> findTop100ByMonthOrderByPostCountDesc(LocalDate month);

    // Custom method to delete entries older than a certain month
    void deleteByMonthBefore(LocalDate month);
    
    // Query to fetch leaderboard for a specific month
    List<MonthlyLeaderboard> findByMonth(LocalDate month);
}
