package com.blog_app.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.blog_app.entities.BlockedUser;
import com.blog_app.entities.Message;
import com.blog_app.entities.MutedChat;
import com.blog_app.entities.User;
import com.blog_app.payloads.MessageDto;
import com.blog_app.repositories.BlockedUserRepo;
import com.blog_app.repositories.MessageRepo;
import com.blog_app.repositories.MutedChatRepo;
import com.blog_app.repositories.UserRepo;

@Controller
public class ChattingController {

	private final SimpMessagingTemplate messagingTemplate;
	private final MessageRepo messageRepo;
	private final UserRepo userRepo;
	private final BlockedUserRepo blockedUserRepo;
	private final MutedChatRepo mutedChatRepo;
	
	public ChattingController(SimpMessagingTemplate messagingTemplate, MessageRepo messageRepo, UserRepo userRepo, BlockedUserRepo blockedUserRepo, MutedChatRepo mutedChatRepo) {
		this.messagingTemplate = messagingTemplate;
		this.messageRepo = messageRepo;
		this.userRepo = userRepo;
		this.blockedUserRepo = blockedUserRepo;
		this.mutedChatRepo = mutedChatRepo;
	}
	
	    @MessageMapping("/chat") // Client sends messages here
		public void SendMessage(MessageDto messageDto) {
			Optional<User> senderopt = userRepo.findById(messageDto.getSenderId());
			Optional<User> recieveropt = userRepo.findById(messageDto.getReceiverId());
			
			if (senderopt.isPresent() && recieveropt.isPresent()) {
				User sender = senderopt.get();
				User reciever = recieveropt.get();
				
				 // 🛑 Check if the receiver has blocked the sender
		        Optional<BlockedUser> blocked = blockedUserRepo.findByBlockedByAndBlockedUser(reciever, sender);
		        if (blocked.isPresent()) {
		            System.out.println("Message blocked: Sender is blocked by the receiver.");
		            return; // Stop sending the message
		        }
				
				Message message = new Message();
				message.setSender(sender);
				message.setReceiver(reciever);
				message.setContent(messageDto.getContent());
				message.setSentAt(LocalDateTime.now());
				message.setSenderProfilePic(sender.getProfilepic());
				
				Message savedMessage = messageRepo.save(message);
				
				MessageDto responseDto = new MessageDto();
				responseDto.setId(savedMessage.getId());
			    responseDto.setContent(savedMessage.getContent());
			    responseDto.setSenderId(savedMessage.getSender().getId());
			    responseDto.setReceiverId(savedMessage.getReceiver().getId());
			    responseDto.setSenderProfilePic(savedMessage.getSenderProfilePic());
			    responseDto.setSentAt(savedMessage.getSentAt());			
			    responseDto.setRead(savedMessage.isRead()); // Include read status

							// 🔇 Muted chat check (Stop sending notifications if muted)
		        Optional<MutedChat> muted = mutedChatRepo.findByUserAndMutedUser(reciever, sender);
		        if (muted.isPresent()) {
		            System.out.println("Notification skipped: Receiver has muted the sender.");
		            return; // Do not send WebSocket notification
		        }
				
				messagingTemplate.convertAndSendToUser(
						reciever.getUsername(),
						"/queue/messages",
						responseDto
						);
				
				// ...and also to the sender.
		        messagingTemplate.convertAndSendToUser(
		                sender.getUsername(),
		                "/queue/messages",
		                responseDto
		        );
			}
		}
	    
	    @MessageMapping("/read")
	    public void handleRead(MessageDto messageDto) {
	        Optional<Message> messageOpt = messageRepo.findById(messageDto.getId());
	        if(messageOpt.isPresent()){
	            Message message = messageOpt.get();
	            message.setRead(true);
	            messageRepo.save(message);

	            // Convert message to DTO (you might create a helper method for this)
	            MessageDto updatedDto = new MessageDto();
	            updatedDto.setId(message.getId());
	            updatedDto.setSenderId(message.getSender().getId());
	            updatedDto.setReceiverId(message.getReceiver().getId());
	            updatedDto.setContent(message.getContent());
	            updatedDto.setSentAt(message.getSentAt());
	            updatedDto.setSenderProfilePic(message.getSenderProfilePic());
	            updatedDto.setRead(true); // Set read status to true

	            // Notify the sender that the message has been read
	            messagingTemplate.convertAndSendToUser(
	                message.getSender().getUsername(),
	                "/queue/read-status",
	                updatedDto
	            );
	        }
	    }

}
