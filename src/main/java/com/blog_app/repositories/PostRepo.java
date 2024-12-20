package com.blog_app.repositories;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.blog_app.entities.Category;
import com.blog_app.entities.Post;
import com.blog_app.entities.User;

public interface PostRepo extends JpaRepository<Post, Integer> {
	
	//#3
	Page<Post> findByUser(User user, Pageable pageable);
	
	//#2
	Page<Post> findByCategory(Category category, Pageable pageable);
	
	//#4
	List<Post> findByTitleContaining(String title);

	//#5
	Page<Post> findByCategoryIn(List<Category> categories, Pageable pageable);

	
	// new methods from here to get nly published post

	
	List<Post> findAllByIsPublishedFalseAndAddedDateBefore(LocalDateTime currentDate);
		
	//in real dis method is not needed
	List<Post> findAllByIsPublishedFalse();
	
	List<Post> findAllByUserAndIsPublishedFalse(User user);
	
	
	//#1
	Page<Post> findAllByIsPublishedTrueAndCloseFriendsOnlyFalseAndUserIsPrivateFalse(Pageable pageable);
	
	//#2 new
    Page<Post> findByCategoryAndIsPublishedTrueAndCloseFriendsOnlyFalseAndUserIsPrivateFalse(Category category, Pageable pageable);
	
	//#3 new
    Page<Post> findByUserAndIsPublishedTrue(User user, Pageable pageable);

    //#4 new
    List<Post> findByTitleContainingAndIsPublishedTrueAndCloseFriendsOnlyFalseAndUserIsPrivateFalse(String title);
    
    //#5 new
    Page<Post> findByCategoryInAndIsPublishedTrueAndCloseFriendsOnlyFalseAndUserIsPrivateFalse(List<Category> categories, Pageable pageable);
    
    //get post by followings of a users and published posts
    Page<Post> findByUserInAndIsPublishedTrue(List<User> users, Pageable pageable);


    //for recommendation
    
    // Fetch top trending posts based on likes, limited by Pageable
    @Query("SELECT p FROM Post p WHERE p.isPublished = true AND p.closeFriendsOnly = false AND p.user.isPrivate = false ORDER BY p.likeCount DESC")
    List<Post> findTopTrendingPosts(Pageable pageable);
    
    // Fetch distinct categories of posts that the user has liked
    @Query("SELECT DISTINCT p.category FROM Post p JOIN p.likes l WHERE l.user.id = :userId")
    List<Category> findLikedCategoriesByUser(@Param("userId") int i);

    // Fetch posts from the liked categories, excluding posts the user already liked
    @Query("SELECT p FROM Post p WHERE p.category IN :categories AND p.id NOT IN (SELECT l.post.id FROM PostLike l WHERE l.user.id = :userId) AND p.isPublished = true AND p.closeFriendsOnly = false AND p.user.isPrivate = false")
    Page<Post> findPostsFromLikedCategories(@Param("categories") List<Category> categories, @Param("userId") int userId, Pageable pageable);


}
