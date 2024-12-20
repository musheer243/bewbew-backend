package com.blog_app.payloads;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MonthlyLeaderboardDto {

	private int id;

    private String username;
    
    private int postCount;
    
    private LocalDate month;
}
