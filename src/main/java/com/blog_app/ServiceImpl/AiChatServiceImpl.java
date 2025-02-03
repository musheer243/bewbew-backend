package com.blog_app.ServiceImpl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.blog_app.services.AiChatService;

import reactor.core.publisher.Flux;
@Service
public class AiChatServiceImpl implements AiChatService {

	private ChatClient chatClient;
	
	public AiChatServiceImpl(ChatClient.Builder builder) {
		this.chatClient = builder.build();
	}
	
	@Override
	public Flux<String> streamResponse(String message){
		return this.chatClient.prompt().user(message).stream().content();
	}
	
}
