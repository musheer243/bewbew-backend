package com.blog_app.services;

import reactor.core.publisher.Flux;

public interface AiChatService {

	Flux<String> streamResponse(String message);

}
