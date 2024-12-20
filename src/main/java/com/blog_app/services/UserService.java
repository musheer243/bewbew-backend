package com.blog_app.services;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.blog_app.entities.User;
import com.blog_app.payloads.PasswordUpdateDto;
import com.blog_app.payloads.PostDto;

//import org.apache.catalina.User;

import com.blog_app.payloads.UserDto;

public interface UserService {

	//UserDto createUser(UserDto user);
	
	//UserDto updateUser(UserDto user, Integer unserId);
	
    UserDto getUserDtoById(Integer userId);
    
    List<UserDto>getAllUser();
    
    void deleteUser(Integer userId);
    
    UserDto createusertWithImage(UserDto userDto);
    
    UserDto updateUserWithProfilePic(String userDto, MultipartFile image, Integer userId) throws IOException;
    
    void updatePassword(String email, PasswordUpdateDto passwordUpdateDto);

	void verifyUser(String email);
	
	void updateUserPreference(String email, String preference);
	
    String getUserPreference(String email);
    
    //search user
    List<UserDto> searchUser(String name);
    
    // Method to toggle account privacy
    void toggleAccountPrivacy(int userId);
    
 // Fetch the list of followers of the user
    Set<UserDto> getFollowers(int userId);
    
 // Fetch the list of users that the user is following
    Set<UserDto> getFollowing(int userId);
    
    //adding users to a specific listcalled closed friend list 
     boolean addCloseFriend(Integer currentUser, int friendId);

	Set<UserDto> getCloseFriends(int userId);

	boolean removeCloseFriend(int userId, int friendId);

	boolean addMultipleCloseFriends(Integer currentUser, List<Integer> friendIds);

	User getLoggedInUser();

}
 