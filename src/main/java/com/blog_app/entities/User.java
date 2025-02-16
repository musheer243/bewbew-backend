package com.blog_app.entities;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.hibernate.annotations.Collate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="user")
@NoArgsConstructor
@Setter
@Getter
public class User implements UserDetails {
	/* primary key */
	@Id  
	/* Auto increment Id */
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="name", nullable = false, length = 100)
	private String name;
	
	@Column(name = "username", nullable = false, unique = true)
	private String username;

	
	@Column(name = "email", nullable = false, unique = true, length = 100)
	private String email;
	
	private String password;
	
	private String about;
	
	
	private String profilepic;
	
	private LocalDateTime joiningdate;
	
	 @Column(name = "is_verified")
	 private boolean isVerified = false;
	 
	 private String userPreference = "light";
	 
	 @Column(name = "oauth_provider")
	 private String oauthProvider; 
	 
	 private String badge = "Beginner";
	 
	// Total posts created by the user
	    @Column(name = "total_posts", nullable = false)
	    private int totalPosts = 0;

	    // Total likes received by the user on all their posts
	    @Column(name = "total_likes", nullable = false)
	    private int totalLikes = 0;
	    
	    @Column(name = "is_private", nullable = false)
	    private boolean isPrivate = false;
	    
	 // Total followers count
	    @Column(name = "total_followers", nullable = false)
	    private int totalFollowers = 0;

	    // Total followings count
	    @Column(name = "total_followings", nullable = false)
	    private int totalFollowings = 0;
	    
	 // Followers of this user
	    @ManyToMany(fetch = FetchType.LAZY)
	    @JoinTable(name = "followers",
	            joinColumns = @JoinColumn(name = "user_id"),
	            inverseJoinColumns = @JoinColumn(name = "follower_id"))
	    @JsonIgnore
	    private Set<User> followers = new HashSet<>();

	    // Following other users
	    @ManyToMany(fetch = FetchType.LAZY)
	    @JoinTable(name = "following",
	            joinColumns = @JoinColumn(name = "follower_id"),
	            inverseJoinColumns = @JoinColumn(name = "user_id"))
	    @JsonIgnore
	    private Set<User> following = new HashSet<>();
	    
	    @ManyToMany(fetch = FetchType.LAZY)
	    @JoinTable(name = "close_friends",
	            joinColumns = @JoinColumn(name = "user_id"),
	            inverseJoinColumns = @JoinColumn(name = "close_friend_id"))
	    @JsonIgnore
	    private Set<User> closeFriends = new HashSet<>();

	    
	 // Follow requests sent to this user (for private accounts)
	    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
	    private Set<FollowRequest> receivedFollowRequests = new HashSet<>();
	    
	 @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	    private List<Reply> replies;
	
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SavedPost> savedPosts = new HashSet<>();
	
	@OneToMany(mappedBy = "user",cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
	private List<Post> posts = new ArrayList<>();
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true)
	private Set<Comment>comments = new HashSet<>();
	
	@JoinTable(name = "user_role",
	joinColumns = @JoinColumn(name = "User",referencedColumnName = "id"),
	inverseJoinColumns = @JoinColumn(name="Role", referencedColumnName = "roleId")
	)
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
	private Set<Role> roles = new HashSet<>();
	
	 @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	 private Set<PostLike> likes = new HashSet<>();  // For user-post likes relationship

	// Notifications this user has sent (e.g., liking/commenting)
	    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
	    private List<Notification> sentNotifications = new ArrayList<>();

	    // Notifications this user has received (e.g., their post was liked/commented on)
	    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
	    private List<Notification> receivedNotifications = new ArrayList<>();

	    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	    private Set<NotInterestedPost> notInterestedPosts = new HashSet<>();

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
	
		List<SimpleGrantedAuthority> authorities = this.roles.stream().map((role)-> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
		return authorities;
	}


	@Override
	public String getUsername() {
		return this.username;
	}
	
	
	public String getEmail() {
		return this.email;
	}
	
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}


}
