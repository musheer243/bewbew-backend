package com.blog_app.ServiceImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.blog_app.exceptions.*;
import com.blog_app.payloads.PasswordUpdateDto;
import com.blog_app.payloads.PostDto;
import com.blog_app.payloads.UserDto;
import com.blog_app.repositories.RoleRepo;
import com.blog_app.repositories.UserRepo;
import com.blog_app.services.FileService;
import com.blog_app.services.OTPService;
import com.blog_app.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.blog_app.exceptions.*;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.s3.endpoints.internal.Substring;

import com.blog_app.config.AppConstants;
import com.blog_app.entities.*;

@Getter
@Setter
@Service
public class UserServiceimpl implements UserService {
	
@Autowired
	private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RoleRepo roleRepo;
    
//    @Value("${project.userimage}")
//	private String path;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private FileService fileService;

//	@Override
//	public UserDto createUser(UserDto userDto) {
//		String password = userDto.getPassword();
//	   User user = this.modelMapper.map(userDto, User.class);
//	   user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
//	   Role role = this.roleRepo.findById(AppConstants.NORMAL_USER).get();
//	   user.getRoles().add(role);
//		User savedUser = this.userRepo.save(user);
//		
//		UserDto userDtos = this.modelMapper.map(savedUser, UserDto.class);
//		userDtos.setPassword(password);
//		return userDtos;
//	}
    
    // Method to get the currently logged-in user
    @Override
    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            // Check if the current authentication is an instance of UserDetails (used in Spring Security)
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                
                // Fetch user by username or email
                return userRepo.findByUsernameOrEmail(username, username)
                    .orElseThrow(() -> new PostUpdateDataNotFoundException("User not found"));
            }
        }
        throw new PostUpdateDataNotFoundException("No authenticated user found");
    }

    @Override
    public UserDto updateUserWithProfilePic(String userDto, MultipartFile image, Integer userId) throws IOException {
        // Fetch the user by ID, or throw an exception if not found
        User user = this.userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Deserialize the userDto string into a UserDto object
        UserDto updatedUserDto = this.objectMapper.readValue(userDto, UserDto.class);

        // Update user details only if provided (except for password)
        if (updatedUserDto.getName() != null && !updatedUserDto.getName().isEmpty()) {
            user.setName(updatedUserDto.getName());
        }
        
        if (updatedUserDto.getUsername() !=null && !updatedUserDto.getUsername().isEmpty()) {
        	if (this.userRepo.existsByUsername(updatedUserDto.getUsername())) {
    	        throw new PostUpdateDataNotFoundException("username is already in use. Please use a different username.");
    	    }
        	user.setUsername(updatedUserDto.getUsername());
        }
//        if (updatedUserDto.getEmail() != null && !updatedUserDto.getEmail().isEmpty()) {
//            user.setEmail(updatedUserDto.getEmail());
//        }
        if (updatedUserDto.getAbout() != null && !updatedUserDto.getAbout().isEmpty()) {
            user.setAbout(updatedUserDto.getAbout());
        }

        // Update profile picture if provided
        if (image != null && !image.isEmpty()) {
            String filename = this.fileService.uploadImage(image);
            user.setProfilepic(filename);
        }

        // Save the updated user
        User updatedUser = this.userRepo.save(user);
        
        // Convert updated user entity back to DTO and return it
        return this.modelMapper.map(updatedUser, UserDto.class);
    }
    

	@Override
	public UserDto getUserDtoById(Integer userId) {
		
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException ("User","id",userId));
		
		return this.userToDto(user);
	}

	@Override
	public List<UserDto> getAllUser() {
		
	List<User> users =	this.userRepo.findAll();
	
	List<UserDto>userDtos = users.stream().map(user->this.userToDto(user)).collect(Collectors.toList());
	
		
		return userDtos;
	 }

	@Override
	public void deleteUser(Integer userId) {
		
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException ("User","id",userId));
		String profilepic = user.getProfilepic();
		
		if(!profilepic.equals("https://bewbew-images-bucket.s3.amazonaws.com/profile_pic.jfif")) {
	    this.fileService.deleteUserFile(profilepic);
		}
		this.userRepo.delete(user);
	}
	
	private User dtoToUser(UserDto userDto) {
		
	User user = this.modelMapper.map(userDto, User.class);
		
//	User user = new User();
//		
//		user.setName(userDto.getName());
//		user.setId(userDto.getId());
//		user.setEmail(userDto.getEmail());
//	    user.setAbout(userDto.getAbout());
//	    user.setPassword(userDto.getPassword());
	    
	    return user;
	}
	
	public UserDto userToDto( User user) {
		
		UserDto userDto = this.modelMapper.map(user, UserDto.class);
		
//		UserDto userDto = new UserDto();
//		
//		userDto.setId(user.getId());
//    	userDto.setName(user.getName());
//	    userDto.setAbout(user.getAbout());
//		userDto.setEmail(user.getEmail());
//		userDto.setPassword(user.getPassword());
		
		return userDto;
		
	}

	@Override
	public UserDto createusertWithImage(UserDto userDto) {

		
		String password = userDto.getPassword();
		
		User user = this.modelMapper.map(userDto, User.class);
		
		user.setPassword(this.passwordEncoder.encode(password));
		
		user.setJoiningdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		
        user.setVerified(true); // Mark user as verified

		
		Role role = this.roleRepo.findById(AppConstants.NORMAL_USER).get();
		user.getRoles().add(role);
		
		
	    
	    User save = this.userRepo.save(user);
	    
	    UserDto map = this.modelMapper.map(save, UserDto.class);
	    map.setPassword(password);
		return map;
	}




	@Override
	public void updatePassword(String email, PasswordUpdateDto passwordUpdateDto) {
		User user = this.userRepo.findByEmail(email).orElseThrow(()-> new SecurityResourceNotFoundException("user", "email Id", email));
		
		// Ensure only traditional users can update passwords
	    if (user.getOauthProvider() != null) {
	        throw new PostUpdateDataNotFoundException("OAuth2 users cannot update passwords");
	    }
	    
	    
		if (!passwordEncoder.matches(passwordUpdateDto.getCurrentPassword(), user.getPassword())) {
            throw new PostUpdateDataNotFoundException("Current password is incorrect");
        }
		
		// Check if the new password and confirm password match
	    if (!passwordUpdateDto.getNewPassword().equals(passwordUpdateDto.getConfirmPassword())) {
	        throw new PostUpdateDataNotFoundException("New password and confirm password do not match");
	    }
		
		user.setPassword(passwordEncoder.encode(passwordUpdateDto.getNewPassword()));
		
		User save = this.userRepo.save(user);
		
	}

	@Override
	public void verifyUser(String email) {
	    User user = this.userRepo.findByEmail(email).orElseThrow(() -> new SecurityResourceNotFoundException("User", "email", email));
	    user.setVerified(true);  // Set isVerified to true
	    this.userRepo.save(user);
	}


	@Override
	public void updateUserPreference(String email, String preference) {
		 User user = userRepo.findByEmail(email).orElseThrow(()-> new PostUpdateDataNotFoundException("User not found with email " + email));
		 user.setUserPreference(preference);
		 userRepo.save(user);
	}


	@Override
	public String getUserPreference(String email) {
		 User user = userRepo.findByEmail(email).orElseThrow(()-> new PostUpdateDataNotFoundException("User not found with email " + email));
		return user.getUserPreference();
	}


	@Override
	public List<UserDto> searchUser(String searchInput) {
	    List<User> users = this.userRepo.findByNameContainingOrUsernameContaining(searchInput, searchInput);
	    List<UserDto> collect = users.stream()
        .map(user -> modelMapper.map(user, UserDto.class))
        .collect(Collectors.toList());
	    return collect;
	}


	@Override
	public void toggleAccountPrivacy(int userId) {
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		
		user.setPrivate(!user.isPrivate());
		userRepo.save(user);
	}


	@Override
	public Set<UserDto> getFollowers(int userId) {
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		Set<User> followers = user.getFollowers();
		 Set<UserDto> collect = followers.stream().map(myFollowers-> modelMapper.map(myFollowers, UserDto.class)).collect(Collectors.toSet());
		return collect;
	}


	@Override
	public Set<UserDto> getFollowing(int userId) {
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		Set<User> following = user.getFollowing();
		Set<UserDto> collect = following.stream().map(myFollowing-> modelMapper.map(myFollowing, UserDto.class)).collect(Collectors.toSet());
		return collect;
	}


	@Override
	public boolean addCloseFriend(Integer currentUser, int friendId) {
		User user = this.userRepo.findById(currentUser).orElseThrow(()-> new ResourceNotFoundException("user", "userId", currentUser));
		User friend = this.userRepo.findById(friendId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", friendId));
		
		if (user.getCloseFriends().contains(friend)) {
			throw new PostUpdateDataNotFoundException("user already exists in close friend");
		}

		if (user.getFollowers().contains(friend)) {
			user.getCloseFriends().add(friend);
			userRepo.save(user);
			return true;	
		}
		return false;
	}
	
	@Override
	public boolean addMultipleCloseFriends(Integer currentUser, List<Integer> friendIds) {
		User user = this.userRepo.findById(currentUser).orElseThrow(()-> new ResourceNotFoundException("user", "userId", currentUser));
		
		for(Integer friendId: friendIds) {
			User friend = this.userRepo.findById(friendId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", friendId));
			
			
			if (user.getCloseFriends().contains(friend)) {
				continue;
				}
			
			if (user.getFollowers().contains(friend)) {
				user.getCloseFriends().add(friend);
			}
		}
		userRepo.save(user);
		return true;
	}
	
	@Override
	public Set<UserDto> getCloseFriends(int userId){
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		
		Set<User> closeFriends = user.getCloseFriends();
		Set<UserDto> collect = closeFriends.stream().map(CloseFriends-> modelMapper.map(CloseFriends, UserDto.class)).collect(Collectors.toSet());
		return collect;
	}
	
	@Override
	public boolean removeCloseFriend(int userId, int friendId) {
		
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		User friend = this.userRepo.findById(friendId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", friendId));

		if (user.getCloseFriends().contains(friend)) {
			user.getCloseFriends().remove(friend);
			userRepo.save(user);
			return true;
		}
		return false;
	}
	
	//method to check whether a user is following a user
	@Override
	public boolean isUserFollowed(int userId, int followerId) {
		User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user", "userId", userId));
		
		return user.getFollowers().stream().anyMatch(follower -> follower.getId() == followerId);
	}
}
	
