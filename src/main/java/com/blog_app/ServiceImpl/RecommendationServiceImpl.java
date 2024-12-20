package com.blog_app.ServiceImpl;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.blog_app.entities.Category;
import com.blog_app.entities.Post;
import com.blog_app.entities.PostLike;
import com.blog_app.entities.User;
import com.blog_app.entities.UserInteraction;
import com.blog_app.repositories.NotInterestedPostRepo;
import com.blog_app.repositories.PostRepo;
import com.blog_app.repositories.UserInteractionRepo;
import com.blog_app.services.MahoutDataLoaderService;
import com.blog_app.services.RecommendationService;
@Service
public class RecommendationServiceImpl implements RecommendationService {

	@Autowired
	private UserInteractionRepo userInteractionRepo;
	
	@Autowired
	private MahoutDataLoaderService mahoutDataLoaderService;
	
	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private NotInterestedPostRepo notInterestedPostRepo;
	
	@Override
	public List<Post> recommendPosts(User user, int howMany) throws Exception {
		
        // Load data from user interactions
		DataModel model = mahoutDataLoaderService.createDataModel();
		
        // Use a UserSimilarity algorithm
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
		
        // Define a neighborhood of users
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, model);

        // Create the recommender
        Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

     // Get recommended post IDs for the given user
        List<RecommendedItem> recommendations = recommender.recommend(user.getId(), howMany);

     // Fetch the posts that the user has marked as "Not Interested"
        Set<Post> notInterestedPosts = this.notInterestedPostRepo.findPostByUser(user);
        
        // Fetch posts that the user has already liked
        Set<Post> likedPosts = user.getLikes().stream().map(PostLike::getPost).collect(Collectors.toSet());

        // Fetch the posts based on recommendation IDs
        List<Post> recommendedPosts = new ArrayList<>();
        for (RecommendedItem recommendation : recommendations) {
            Optional<UserInteraction> userInteraction = userInteractionRepo.findById((long) recommendation.getItemID());
            
         // Get the associated Post from UserInteraction
            userInteraction.ifPresent(interaction -> {
                Post post = interaction.getPost();  // Assuming UserInteraction has a getPost() method
                
                if (post.isPublished() && !post.isCloseFriendsOnly() && !post.getUser().isPrivate() && !likedPosts.contains(post) && 
                        !notInterestedPosts.contains(post)){				
                recommendedPosts.add(post);
                }
            });
        }

        return recommendedPosts;
	}
	
	// Method to get recommended posts
	@Override
    public List<Post> getRecommendationsForUser(User user, int trendingLimit, int categoryLimit) {
        // Fetch trending posts (most liked)
        List<Post> trendingPosts = postRepo.findTopTrendingPosts(PageRequest.of(0, trendingLimit));

        // Fetch categories of posts the user has liked
        List<Category> likedCategories = postRepo.findLikedCategoriesByUser(user.getId());

        // Fetch posts from those liked categories, excluding posts already liked by the user
        Page<Post> categoryPostsPage = postRepo.findPostsFromLikedCategories(likedCategories, user.getId(), PageRequest.of(0, categoryLimit));
        
     // Fetch posts that the user has marked as "Not Interested"
        Set<Post> notInterestedPosts = this.notInterestedPostRepo.findPostByUser(user);
        
        // Fetch posts that the user has already liked
        Set<Post> likedPosts = user.getLikes().stream().map(PostLike::getPost).collect(Collectors.toSet());


        Set<Post> recommendedPosts = new HashSet<>();
        
        recommendedPosts.addAll(trendingPosts.stream()
                .filter(post -> !likedPosts.contains(post) && !notInterestedPosts.contains(post))  // Exclude "not interested" and liked posts
                .collect(Collectors.toList()));
            
            recommendedPosts.addAll(categoryPostsPage.getContent().stream()
                .filter(post -> !likedPosts.contains(post) && !notInterestedPosts.contains(post))  // Exclude "not interested" and liked posts
                .collect(Collectors.toList()));
        // Return combined result
        return new ArrayList<>(recommendedPosts); // Convert back to List
    }

}
