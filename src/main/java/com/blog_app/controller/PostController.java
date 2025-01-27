package com.blog_app.controller;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.angus.mail.imap.protocol.BODY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.blog_app.config.AppConstants;
import com.blog_app.payloads.ApiResponse;
import com.blog_app.payloads.PostDto;
import com.blog_app.payloads.PostResponse;
import com.blog_app.payloads.UserDto;
import com.blog_app.services.FileService;
import com.blog_app.services.PostLikeService;
import com.blog_app.services.PostService;
import com.blog_app.services.SavedPostService;
import com.blog_app.services.TranslationService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class PostController {
	
	@Value("${app.base.url}")  // Inject the base URL from application.properties
    private String baseUrl;

	@Autowired
	private PostLikeService postLikeService;
	
	@Autowired
	private SavedPostService savedPostService;
	
	@Autowired
	private PostService postService;
	
	@Autowired
	private FileService fileService;
	
	@Value("${project.image}")
	private String path;
	
	@Autowired
	private TranslationService translationService;
	

	
	//get by user
	 @GetMapping("/user/{userId}/posts")
		public ResponseEntity <PostResponse> getPostByUser(
				@PathVariable Integer userId,
				@RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false)Integer pageNumber,
				@RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false)Integer pageSize,
				@RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false)String sortBy,
				@RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false)String sortDir
				){
			
		PostResponse posts =this.postService.getPostByUser(userId, pageNumber, pageSize, sortBy, sortDir);
			
		return new ResponseEntity<PostResponse>(posts,HttpStatus.OK);
		}
	
	
	//get by category
	
	@GetMapping("/category/{categoryId}/posts")
	public ResponseEntity<PostResponse> getPostByCategory(
			        @PathVariable Integer categoryId,
					@RequestParam(value = "pageNumber", defaultValue =  AppConstants.PAGE_NUMBER, required = false)Integer pageNumber,
					@RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false)Integer pageSize,
					@RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false)String sortBy,
					@RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false)String sortDir
					){
				 PostResponse postResponse = this.postService.getPostByCategory(categoryId, pageNumber,  pageSize, sortBy, sortDir);
				return new ResponseEntity<PostResponse>(postResponse, HttpStatus.OK);
			}	
	
	
	// Method to get posts by searching category title
    @GetMapping("/post/search-by/category")
    public ResponseEntity<PostResponse> getPostsByCategoryTitle(
            @RequestParam(value = "categoryTitle") String categoryTitle,
            @RequestParam(value = "pageNumber", defaultValue =  AppConstants.PAGE_NUMBER, required = false)Integer pageNumber,
			@RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false)Integer pageSize,
			@RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false)String sortBy,
			@RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false)String sortDir
			) {

        PostResponse postResponse = this.postService.getPostsByCategoryTitle(categoryTitle, pageNumber, pageSize, sortBy, sortDir);
        
        return ResponseEntity.ok(postResponse);
    }
    
    
	//delete post
	
	@DeleteMapping("/posts/{postId}")
	public ApiResponse deletePost(@PathVariable Integer postId) {
		
		this.postService.deletePost(postId);
	    return new ApiResponse("Post is successfully deleted", true);
		
	}
	
	
		
		
	
	//GET all post
	
		@GetMapping("/posts")
		public ResponseEntity<PostResponse> getAllPost(
				@RequestParam(value = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false)int pageNumber,
				@RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE,required = false)int pageSize,
				@RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false)String sortBy,
				@RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false)String sortDir
				){
			PostResponse getAllPost = this.postService.getAllPost( pageNumber,  pageSize, sortBy, sortDir);
			return new ResponseEntity<PostResponse>(getAllPost,HttpStatus.OK);
			
		}
		
		//get by post id 
		
		@GetMapping("/post/view/{postId}")
		
		public ResponseEntity<PostDto> getPostById(@PathVariable Integer postId){
			return ResponseEntity.ok(this.postService.getPostById(postId));      
			
		}
		 //share post
	    @GetMapping("/post/share/{postId}")
	    public ResponseEntity<String> sharePost(@PathVariable int postId) {
	        // Generate shareable link (e.g., https://yourapp.com/posts/share/{postId})
	        String shareLink = baseUrl + "/api/post/view/" + postId;
	        return new ResponseEntity<String>(shareLink,HttpStatus.OK);
	    }
		
		//Search Post
		
		@GetMapping("/posts/search/{keyword}")
		
		public ResponseEntity<List<PostDto>> searchPosts(@PathVariable String keyword){
			
			List<PostDto> result = this.postService.searchPosts(keyword);
			
			return new ResponseEntity<List<PostDto>> (result,HttpStatus.OK);
		}
		
		
		

		@PostMapping("/user/{userId}/category/{categoryId}/posts/with-media")
		public ResponseEntity<PostDto> createPostWithMedia(
		        @RequestParam("postDto") String postDto,
		        @RequestParam(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
		        @PathVariable Integer userId,
		        @PathVariable Integer categoryId
		) throws IOException {
		    // Call service to create post and upload media files (images/videos)
		    PostDto createdPost = this.postService.createPostWithMedia(postDto, userId, categoryId, mediaFiles);
		    return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
		}
		
		
		//get media
		
		@GetMapping(value = "/post/media")
		public void downloadMedia(
		        @RequestParam("fileNames") List<String> fileNames,
		        HttpServletResponse response) throws IOException {

		    for (String fileName : fileNames) {
		        InputStream resource;

		        // Check if the fileName is a URL (for S3)
		        if (fileName.startsWith("http") || fileName.startsWith("https")) {
		            // Redirect to the S3-hosted image
		            response.sendRedirect(fileName);
		            return; // Exit if redirected
		        } else {
		            // Fallback for serving local images
		            resource = this.fileService.getResource(fileName);

		            if (resource == null) {
		                throw new FileNotFoundException("File not found: " + fileName);
		            }
		        }

		        try {
		            // Determine file extension and set media type
		            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		            String mediaType;

		            switch (fileExtension) {
		            // Image formats
		            case "png":
		                mediaType = MediaType.IMAGE_PNG_VALUE;
		                break;
		            case "jpeg":
		            case "jpg":
		                mediaType = MediaType.IMAGE_JPEG_VALUE;
		                break;
		            case "gif":
		                mediaType = MediaType.IMAGE_GIF_VALUE;
		                break;
		            case "bmp":
		                mediaType = "image/bmp";  // BMP format doesn't have a predefined constant in MediaType
		                break;
		            case "tiff":
		                mediaType = "image/tiff";  // TIFF format doesn't have a predefined constant in MediaType
		                break;
		            case "webp":
		                mediaType = "image/webp";  // WEBP format doesn't have a predefined constant in MediaType
		                break;
		            case "jfif":
		                mediaType = "image/jfif";  // JFIF format doesn't have a predefined constant
		                break;
		            case "svg":
		            case "svg+xml":
		                mediaType = "image/svg+xml";  // Hardcoded SVG content type
		                break;
		                
		            // Video formats
		            case "mp4":
		                mediaType = "video/mp4";  // MP4 video format
		                break;
		            case "avi":
		                mediaType = "video/avi";  // AVI video format
		                break;
		            case "mpeg":
		                mediaType = "video/mpeg";  // MPEG video format
		                break;
		            case "quicktime":
		            case "mov":
		                mediaType = "video/quicktime";  // MOV (QuickTime) format
		                break;
		            case "x-ms-wmv":
		                mediaType = "video/x-ms-wmv";  // WMV format
		                break;
		            case "x-flv":
		                mediaType = "video/x-flv";  // FLV format
		                break;
		            case "webm":
		                mediaType = "video/webm";  // WEBM format
		                break;

		            default:
		                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;  // Fallback for unknown types
		                break;
		        }


		            // Set content type and length
		            response.setContentType(mediaType);
		            response.setContentLengthLong(resource.available());

		            // Stream the file
		            StreamUtils.copy(resource, response.getOutputStream());

		        } catch (IOException e) {
		            throw new RuntimeException("Error while copying stream", e);
		        } finally {
		            // Ensure the InputStream is closed
		            if (resource != null) {
		                resource.close();
		            }
		        }
		    }
		}

		
		

		@PutMapping("/post/{postId}/updateWithMedia")
		public ResponseEntity<PostDto> updatePostWithMedia(
				@PathVariable Integer postId,
				@RequestPart(value = "postDto",required = false)String postDto,
				@RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles)throws IOException{
					
			PostDto updatePostWithMedia = this.postService.updatePostWithMedia(postId, postDto, mediaFiles);
					 
			return new ResponseEntity<PostDto>(updatePostWithMedia,HttpStatus.OK);
		}


		@PostMapping("/post/{postId}/like")
	    public ResponseEntity<String> toggleLikePost(@PathVariable int postId,
	    		@RequestParam int userId) {
	        String response = postLikeService.toggleLikePost(postId, userId);
	        return new ResponseEntity<String>(response,HttpStatus.OK);
	    }
		
		//get boolean for if user has liked a specific post or not
		@GetMapping("/post/{postId}/isLiked")
		public ResponseEntity<Boolean> isPostLiked(@PathVariable int postId, @RequestParam int userId) {
		    boolean isLiked = postLikeService.isPostLikedByUser(postId, userId);
		    return ResponseEntity.ok(isLiked);
		}
		
		 // New endpoint to get my liked posts
	    @GetMapping("/post/user/{userId}/liked-posts")
	    public ResponseEntity<PostResponse> getLikedPosts(@PathVariable Integer userId,
				@RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false)Integer pageNumber,
				@RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false)Integer pageSize,
				@RequestParam(value = "sortBy", defaultValue = "id", required = false)String sortBy,
				@RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false)String sortDir
				) {
	         PostResponse likedPostsByUser = postLikeService.getLikedPostsByUser(userId, pageNumber, pageSize, sortBy, sortDir);
	        return new ResponseEntity<>(likedPostsByUser, HttpStatus.OK);
	        
	    }
	    
	   
		
	    
	    //to get users who liked the a specific post
	    @GetMapping("/post/{postId}/likedUsers")
	    public ResponseEntity<List<UserDto>> getUsersWhoLikedPost(@PathVariable int postId){
	    	List<UserDto> usersWhoLikedPost = this.postLikeService.getUsersWhoLikedPost(postId);
	    	
	    	return new ResponseEntity<List<UserDto>>(usersWhoLikedPost,HttpStatus.OK);
	    }

	    
		
		@PostMapping("/post/{postId}/save")
	    public ResponseEntity<String> toggleSavedPost(@PathVariable int postId,
	    		@RequestParam int userId) {
	        String response = this.savedPostService.toggleSavedPost(postId, userId);
	        return new ResponseEntity<String>(response,HttpStatus.OK);
	    }
		
		//get boolean for if user has Saved a specific post or not
				@GetMapping("/post/{postId}/isSaved")
				public ResponseEntity<Boolean> isPostSaved(@PathVariable int postId, @RequestParam int userId) {
					boolean postSavedByUser = savedPostService.isPostSavedByUser(postId, userId);
					return ResponseEntity.ok(postSavedByUser);
				}
		// New endpoint to get my saved posts
	    @GetMapping("/post/user/{userId}/saved-posts")
	    public ResponseEntity<PostResponse> getSavedPosts(@PathVariable Integer userId,
				@RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false)Integer pageNumber,
				@RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false)Integer pageSize,
				@RequestParam(value = "sortBy", defaultValue = "id", required = false)String sortBy,
				@RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false)String sortDir
				) {
	         PostResponse savedPostsByUser = savedPostService.getSavedPostsByUser(userId, pageNumber, pageSize, sortBy, sortDir);
	        return new ResponseEntity<>(savedPostsByUser, HttpStatus.OK);
	    }
		
	    //get all scheduled posts
	    @GetMapping("/post/scheduled")
	    public ResponseEntity<List<PostDto>> getScheduledPosts() {
	         List<PostDto> scheduledPosts = postService.getScheduledPosts();
	         return new ResponseEntity<List<PostDto>>(scheduledPosts,HttpStatus.OK);
	    }
		
	    //get my scheduled posts
	    @GetMapping("/post/users/{userId}/scheduled-posts")
	    public ResponseEntity<List<PostDto>> getScheduledPostsForUser(@PathVariable Integer userId) {
	        List<PostDto> scheduledPosts = postService.getScheduledPostsForUser(userId);
	        return ResponseEntity.ok(scheduledPosts);
	    }
	    
	    //post title and content translation
	    @PostMapping("/post/{postId}/translate")
	    public ResponseEntity<PostDto> translatePost(@PathVariable int postId, @RequestParam String language) {
	        PostDto postDto = postService.getPostById(postId);
	        String translatedTitle = translationService.translateText(postDto.getTitle(), language);
	        String translatedContent = translationService.translateText(postDto.getContent(), language);
	        postDto.setTitle(translatedTitle);
	        postDto.setContent(translatedContent);
	        return ResponseEntity.ok(postDto);
	    }
	    
	    @GetMapping("/post/byfollowing/{userId}")
	    public ResponseEntity<PostResponse> getPostsByFollowingUsers(
	            @PathVariable Integer userId,
	            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) int pageNumber,
	            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) int pageSize,
	            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
	            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir) {

	        PostResponse postsByFollowing = postService.getPostsByUsersFollowing(userId, pageNumber, pageSize, sortBy, sortDir);
	        return new ResponseEntity<>(postsByFollowing, HttpStatus.OK);
	    }

}
