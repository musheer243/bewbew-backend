package com.blog_app.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.blog_app.entities.User;
import com.blog_app.payloads.ApiResponse;
import com.blog_app.payloads.PasswordUpdateDto;
import com.blog_app.payloads.UserDto;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.EmailService;
import com.blog_app.services.FileService;
import com.blog_app.services.OTPService;
import com.blog_app.services.UserService;

import io.jsonwebtoken.Jwt;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

//import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private OTPService otpService;
	
	@Autowired
	private UserRepo userRepo;


	
	//PUT Update user details and profile picture
	@PutMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserDto> updateUserWithProfilePic(
	    @PathVariable("userId") Integer userId,
	    @RequestPart(value = "userDto", required = false) String userDto, 
	    @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

	    // Call the service method to update user details and/or profile picture
	    UserDto updatedUser = this.userService.updateUserWithProfilePic(userDto, image, userId);

	    return ResponseEntity.ok(updatedUser);
	}
	
	
	//update user password
	@PutMapping("/update-password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestBody PasswordUpdateDto passwordUpdateDto) {
		    // Get the current logged-in user
		    User loggedInUser = userService.getLoggedInUser();
		    String email = loggedInUser.getEmail();
        userService.updatePassword(email, passwordUpdateDto);

        return new ResponseEntity<ApiResponse>(new ApiResponse("passwords updated sucessfully",true),HttpStatus.OK);
    }
	
	
	//DELETE user user
	
	@DeleteMapping("/{userId}")
	public ResponseEntity<ApiResponse> deleteUser(@PathVariable("userId")Integer uid)
	{
		//ResponseEntity<?> deleteUser = this.deleteUser(uid);
		this.userService.deleteUser(uid);
		return new ResponseEntity<ApiResponse>(new ApiResponse("User deleted Successfully", true), HttpStatus.OK);
	}
	
	//GET multiple users
	
	@GetMapping("/")
	public ResponseEntity<List<UserDto>> getAllUser(){
		return ResponseEntity.ok(this.userService.getAllUser());
		
	}
	
	//Single user
	
	@GetMapping("/{userId}")
	public ResponseEntity<UserDto> getSingleUser(@PathVariable Integer userId){
		return ResponseEntity.ok(this.userService.getUserDtoById(userId));      //Used getUserDtoById instead of getUserById
		
	}
	
	//method to serve user profilepic
			@GetMapping(value = "profilepic/image/")
			public void getProfilePic(@RequestParam("url") String imageUrl, HttpServletResponse response) throws IOException {
			    
			    // Validate if the imageUrl is a valid URL (Google OAuth users)
			    if (imageUrl.startsWith("http") || imageUrl.startsWith("https")) {
			        // Redirect to the Google-hosted profile image
			        response.sendRedirect(imageUrl);
			        return;
			    }
			    
			    // Fallback for serving local images
			    // If imageUrl is not a URL (for example, for non-OAuth users, you can serve it from your local storage)
			    // Assuming `imageUrl` is the filename stored in your local server
			    InputStream imageStream = this.fileService.getResource(imageUrl);

			    // Determine content type (based on the file extension)
			    String fileExtension = imageUrl.substring(imageUrl.lastIndexOf(".") + 1).toLowerCase();
			    String contentType;

			    switch (fileExtension) {
			    case "png":
			        contentType = MediaType.IMAGE_PNG_VALUE;
			        break;
			    case "jpeg":
			    case "jpg":
			        contentType = MediaType.IMAGE_JPEG_VALUE;
			        break;
			    case "gif":
			        contentType = MediaType.IMAGE_GIF_VALUE;
			        break;
			    case "jfif":
			        contentType = "image/jfif";  // JFIF doesn't have a predefined constant
			        break;
			    case "bmp":
			        contentType = "image/bmp";  // BMP doesn't have a predefined constant in MediaType
			        break;
			    case "tiff":
			        contentType = "image/tiff";  // TIFF doesn't have a predefined constant in MediaType
			        break;
			    case "webp":
			        contentType = "image/webp";  // WEBP doesn't have a predefined constant in MediaType
			        break;
			    case "svg":
			    case "svg+xml":
			        contentType = "image/svg+xml";  // Hardcoded SVG content type
			        break;
			    default:
			        contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // fallback for unknown types
			}


			    // Set content type in response
			    response.setContentType(contentType);

			    // Send image to the response
			    org.springframework.util.StreamUtils.copy(imageStream, response.getOutputStream());

			    // Close the input stream after use
			    imageStream.close();
			}


			@PutMapping("/update-preference")
		    public ResponseEntity<String> updateUserPreference(
		            @RequestParam("email") String email,
		            @RequestParam("preference") String preference) {

		        try {
		            userService.updateUserPreference(email, preference);
		            return new ResponseEntity<>("Preference updated successfully!", HttpStatus.OK);
		        } catch (Exception e) {
		            return new ResponseEntity<>("Error updating preference.", HttpStatus.INTERNAL_SERVER_ERROR);
		        }
		    }
			
			
			//search user
			@GetMapping("/search/{name}")
			public ResponseEntity<List<UserDto>> searchUser(@PathVariable String name){
				List<UserDto> searchedUser = this.userService.searchUser(name);
				return new ResponseEntity<List<UserDto>>(searchedUser,HttpStatus.OK);
			}
			
			@PostMapping("/{userId}/toggle-privacy")
			public ResponseEntity<String> toggleAccountPrivacy(@PathVariable int userId){
				this.userService.toggleAccountPrivacy(userId);
				return new ResponseEntity<String>("Account privacy updated",HttpStatus.OK);
			}
			
			
			// Get the list of followers for a specific user
		    @GetMapping("/{userId}/myfollowers")
		    public ResponseEntity<Set<UserDto>> getFollowers(@PathVariable("userId") int userId) {
		        Set<UserDto> followers = userService.getFollowers(userId);
		        return ResponseEntity.ok(followers);
		    }

		    // Get the list of users the specified user is following
		    @GetMapping("/{userId}/myfollowings")
		    public ResponseEntity<Set<UserDto>> getFollowing(@PathVariable("userId") int userId) {
		        Set<UserDto> following = userService.getFollowing(userId);
		        return ResponseEntity.ok(following);
		    }

}

