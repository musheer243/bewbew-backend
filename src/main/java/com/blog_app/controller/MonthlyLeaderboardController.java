package com.blog_app.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.entities.MonthlyLeaderboard;
import com.blog_app.payloads.MonthlyLeaderboardDto;
import com.blog_app.repositories.MonthlyLeaderboardRepo;

@RestController
@RequestMapping("/api")
public class MonthlyLeaderboardController {
	
	@Autowired
	private MonthlyLeaderboardRepo monthlyLeaderboardRepo;
	
	@Autowired
	private ModelMapper modelMapper;

	@GetMapping("/leaderboard/monthly")
	public ResponseEntity<List<MonthlyLeaderboardDto>> getMonthlyLeaderboard(){
		 // Get the current month (first day)
        LocalDate currentMonth = LocalDate.now(ZoneId.of("Asia/Kolkata")).withDayOfMonth(1);
        
        List<MonthlyLeaderboard> top100ByMonthOrderByPostCountDesc = this.monthlyLeaderboardRepo.findTop100ByMonthOrderByPostCountDesc(currentMonth);

        List<MonthlyLeaderboardDto> collect = top100ByMonthOrderByPostCountDesc.stream().map(top100 -> modelMapper.map(top100, MonthlyLeaderboardDto.class)).collect(Collectors.toList());

        return new ResponseEntity<List<MonthlyLeaderboardDto>>(collect,HttpStatus.OK);
	}
	
	// Get leaderboard for a specific previous month (e.g., 1 month ago, 2 months ago, etc.)
		@GetMapping("/leaderboard/previous/{monthsAgo}")
		public ResponseEntity<List<MonthlyLeaderboardDto>> getPreviousLeaderboard(@PathVariable int monthsAgo) {
			// Ensure the request is valid (1-3 months ago)
			if (monthsAgo < 1 || monthsAgo > 3) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			// Calculate the date for `monthsAgo` months before the current month
			LocalDate targetMonth = LocalDate.now(ZoneId.of("Asia/Kolkata")).minusMonths(monthsAgo).withDayOfMonth(1);

			// Fetch top 100 users for the specified month, ordered by post count
			List<MonthlyLeaderboard> top100ByMonthOrderByPostCountDesc = 
	            this.monthlyLeaderboardRepo.findTop100ByMonthOrderByPostCountDesc(targetMonth);
			
			List<MonthlyLeaderboardDto> collect = top100ByMonthOrderByPostCountDesc.stream().map(top100 -> modelMapper.map(top100, MonthlyLeaderboardDto.class)).collect(Collectors.toList());

			return new ResponseEntity<>(collect, HttpStatus.OK);
		}
}
