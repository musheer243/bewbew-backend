package com.blog_app.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.entities.BlockedUser;
import com.blog_app.entities.Message;
import com.blog_app.entities.MutedChat;
import com.blog_app.entities.User;
import com.blog_app.payloads.MessageDto;
import com.blog_app.payloads.UserDto;
import com.blog_app.repositories.BlockedUserRepo;
import com.blog_app.repositories.MessageRepo;
import com.blog_app.repositories.MutedChatRepo;
import com.blog_app.repositories.UserRepo;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
	
	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private MessageRepo messageRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private BlockedUserRepo blockedUserRepo;
	
	@Autowired
	private MutedChatRepo mutedChatRepo;

	//***
	//to get users recent chats
	@GetMapping("/{userId}/chats")
	public ResponseEntity<List<UserDto>> getRecentChats(@PathVariable int userId) {
	    Optional<User> user = userRepo.findById(userId);
	    if (user.isEmpty()) return ResponseEntity.notFound().build();
	    
	    List<Message> messages = messageRepo.findDistinctBySenderOrReceiver(user.get(), user.get());
	    Set<User> chatPartners = new HashSet<>();
	    
	    for (Message message : messages) {
	        if (message.getSender().getId() != userId) 
	            chatPartners.add(message.getSender());
	        if (message.getReceiver().getId() != userId)
	            chatPartners.add(message.getReceiver());
	    }
	    
	    // Convert to DTOs using model mapper
	    List<UserDto> userDtos = chatPartners.stream()
	        .map(chatPartner -> modelMapper.map(chatPartner, UserDto.class))
	        .collect(Collectors.toList());
	    
	    return ResponseEntity.ok(userDtos);
	}
	
	//no-need
	//get chat history
	@GetMapping("/{userId1}/{userId2}")
	public ResponseEntity<List<MessageDto>> getChatHistory(@PathVariable int userId1, @PathVariable int userId2) {
	    Optional<User> user1 = userRepo.findById(userId1);
	    Optional<User> user2 = userRepo.findById(userId2);

	    if (user1.isPresent() && user2.isPresent()) {
	        List<Message> messages = messageRepo.findBySenderAndReceiverOrReceiverAndSender(
	                user1.get(), user2.get(), user1.get(), user2.get()
	        );

	        // Convert to DTOs and exclude messages marked as deleted
	        List<MessageDto> messageDtos = messages.stream()
	                .filter(message -> !message.getDeletedFor().contains(user1.get().getId())) // Exclude deleted messages
	                .map(message -> {
	                    MessageDto dto = new MessageDto();
	                    dto.setSenderId(message.getSender().getId());
	                    dto.setReceiverId(message.getReceiver().getId());
	                    dto.setContent(message.getContent());
	                    dto.setSentAt(message.getSentAt());
	                    dto.setSenderProfilePic(message.getSender().getProfilepic());
	                    dto.setRead(message.isRead()); // Add isRead status
	                    return dto;
	                })
	                .collect(Collectors.toList());

	        return ResponseEntity.ok(messageDtos);
	    }

	    return ResponseEntity.notFound().build();
	}
	
	//***
	//get chat history in pageable format
	@GetMapping("/page/{userId1}/{userId2}")
	public ResponseEntity<Page<MessageDto>> getChatHistory(
	        @PathVariable int userId1, 
	        @PathVariable int userId2,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "20") int size) {
	    
	    Optional<User> user1 = userRepo.findById(userId1);
	    Optional<User> user2 = userRepo.findById(userId2);

	    if (user1.isPresent() && user2.isPresent()) {
	        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
	        Page<Message> messages = messageRepo.findChatHistory(user1.get(), user2.get(), pageable);

	        Page<MessageDto> messageDtos = messages.map(message -> {
	            MessageDto dto = new MessageDto();
	            dto.setId(message.getId());  // <--- Add this line!
	            dto.setSenderId(message.getSender().getId());
	            dto.setReceiverId(message.getReceiver().getId());
	            dto.setContent(message.getContent());
	            dto.setSentAt(message.getSentAt());
	            dto.setSenderProfilePic(message.getSender().getProfilepic());
	            dto.setRead(message.isRead()); // Add isRead status

	            return dto;
	        });

	        return ResponseEntity.ok(messageDtos);
	    }

	    return ResponseEntity.notFound().build();
	}


	
	//delete a single msg in chat
	@DeleteMapping("/delete/{messageId}")
	public ResponseEntity<String> deleteMessage(@PathVariable int messageId) {
	    Optional<Message> messageOpt = messageRepo.findById(messageId);
	    if (messageOpt.isPresent()) {
	        messageRepo.deleteById(messageId);
	        return ResponseEntity.ok("Message deleted successfully");
	    }
	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found");
	}
	
	//delete chats from one side
	@DeleteMapping("/delete/chat/{userId1}/{userId2}")
	public ResponseEntity<String> deleteChatForUser(@PathVariable int userId1, @PathVariable int userId2) {
	    Optional<User> user1 = userRepo.findById(userId1);
	    Optional<User> user2 = userRepo.findById(userId2);

	    if (user1.isPresent() && user2.isPresent()) {
	        List<Message> messages = messageRepo.findBySenderAndReceiverOrReceiverAndSender(
	            user1.get(), user2.get(), user1.get(), user2.get()
	        );

	        for (Message message : messages) {
	            message.getDeletedFor().add(userId1); // Mark messages as deleted for userId1 only
	        }

	        messageRepo.saveAll(messages);
	        return ResponseEntity.ok("Chat deleted for user " + userId1 + " only");
	    }

	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Users not found");
	}

	
	//delete entire chat histiory from bth side 
	@DeleteMapping("/delete/fullchat/{userId1}/{userId2}")
	public ResponseEntity<String> deleteChat(@PathVariable int userId1, @PathVariable int userId2) {
	    Optional<User> user1 = userRepo.findById(userId1);
	    Optional<User> user2 = userRepo.findById(userId2);

	    if (user1.isPresent() && user2.isPresent()) {
	        List<Message> messages = messageRepo.findBySenderAndReceiverOrReceiverAndSender(
	            user1.get(), user2.get(), user1.get(), user2.get()
	        );
	        messageRepo.deleteAll(messages);
	        return ResponseEntity.ok("Chat history deleted successfully");
	    }

	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Users not found");
	}
	
	//block a user
	@PostMapping("/block/{blockerId}/{blockedUserId}")
	public ResponseEntity<String> blockUser(@PathVariable int blockerId, @PathVariable int blockedUserId) {
	    Optional<User> blocker = userRepo.findById(blockerId);
	    Optional<User> blockedUser = userRepo.findById(blockedUserId);

	    if (blocker.isPresent() && blockedUser.isPresent()) {
	        BlockedUser blocked = new BlockedUser();
	        blocked.setBlockedBy(blocker.get());
	        blocked.setBlockedUser(blockedUser.get());

	        blockedUserRepo.save(blocked);
	        return ResponseEntity.ok("User blocked successfully");
	    }

	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
	}

	//mute a chat notifictions
	@PostMapping("/mute/{userId}/{mutedUserId}")
	public ResponseEntity<String> muteChat(@PathVariable int userId, @PathVariable int mutedUserId) {
	    Optional<User> user = userRepo.findById(userId);
	    Optional<User> mutedUser = userRepo.findById(mutedUserId);

	    if (user.isPresent() && mutedUser.isPresent()) {
	        MutedChat mutedChat = new MutedChat();
	        mutedChat.setUser(user.get());
	        mutedChat.setMutedUser(mutedUser.get());

	        mutedChatRepo.save(mutedChat);
	        return ResponseEntity.ok("Chat muted successfully");
	    }

	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
	}
	
	//mark msges as read 
	@PutMapping("/mark-as-read/{messageId}")
	public ResponseEntity<String> markMessageAsRead(@PathVariable int messageId) {
	    Optional<Message> messageOpt = messageRepo.findById(messageId);
	    if (messageOpt.isPresent()) {
	        Message message = messageOpt.get();
	        message.setRead(true);
	        messageRepo.save(message);
	        return ResponseEntity.ok("Message marked as read");
	    }
	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found");
	}


}
