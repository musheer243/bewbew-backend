package com.blog_app.entities;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name ="post")

@Getter
@Setter
@NoArgsConstructor

public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int postId;
	
	@Column(name = "post_title", length = 100, nullable = false)
	private String title;
	
	@Column(length = 10000)
	private String content;
	

	@Column(length = 1000)	
	private List<String> mediaFileNames;

	
	//@DateTimeFormat(pattern="yyyy-MM-dd")
	//@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Kolkata")
	private LocalDateTime addedDate;
	
	private int likeCount = 0;
	
	private int saveCount = 0;
	
	private int commentCount = 0;
	
	@Column(name = "is_published")
	private boolean isPublished = false;

	
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SavedPost> savedPosts = new HashSet<>();
	
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;
	
	@ManyToOne
	private User user;
	
	@Column(name = "close_friends_only")
    private boolean closeFriendsOnly = false;
	
	
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	private Set<Comment>comments = new HashSet<>();
	
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PostLike> likes = new HashSet<>();

}