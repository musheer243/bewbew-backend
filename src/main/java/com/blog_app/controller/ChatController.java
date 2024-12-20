package com.blog_app.controller;

import java.util.Map;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/")
public class ChatController {

    private final OllamaChatModel chatModel;

    @Autowired
    public ChatController(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "hii") String message) {
        return Map.of("generation", chatModel.call(message));
    }

//    @GetMapping("/ai/generateStream")
//	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "hii") String message) {
//        Prompt prompt = new Prompt(new UserMessage(message));
//        return chatModel.stream(prompt);
//        //return chatModel.stream(prompt);
//    }

}
