package com.blog_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.blog_app.Security.JwtTokenHelper;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenHelper jwtTokenHelper;
    private final UserDetailsService userDetailsService;

    public WebSocketConfig(JwtTokenHelper jwtTokenHelper, UserDetailsService userDetailsService) {
        this.jwtTokenHelper = jwtTokenHelper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("http://localhost:3000"); // Use allowedOriginPatterns
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    System.out.println("WebSocket Headers: " + accessor.toMap()); // Log headers
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        System.out.println("Token received: " + token); // Log token
                        String username = jwtTokenHelper.getUsernameFromToken(token);
                        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            System.out.println("Username extracted: " + username); // Log username
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            if (jwtTokenHelper.validateToken(token, userDetails)) {
                                System.out.println("Token validated for user: " + username); // Log successful validation
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails((HttpServletRequest) accessor.getUser()));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                accessor.setUser(authentication);
                                System.out.println("User authenticated: " + username); // Log successful authentication
                            } else {
                                System.out.println("Invalid token for user: " + username); // Log invalid token
                            }
                        } else {
                            System.out.println("Username is null or user is already authenticated"); // Log username issue
                        }
                    } else {
                        System.out.println("Authorization header is missing or invalid"); // Log missing/invalid header
                    }
                }
                return message;
            }
        });
    }
}