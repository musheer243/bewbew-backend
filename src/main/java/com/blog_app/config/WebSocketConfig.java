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
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("http://localhost:3000")
        .withSockJS(); // Use allowedOriginPatterns
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = 
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 1. Check STOMP headers first
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    
                    // 2. If Authorization header is missing, check query parameters
                    if ((authHeader == null || !authHeader.startsWith("Bearer "))) {
                        // Extract token from the SockJS URL query parameters
                        String query = accessor.getNativeHeader("token").get(0); // Get the first token value
                        if (query != null && !query.isEmpty()) {
                            authHeader = "Bearer " + query;
                        }
                    }

                    // 3. Validate the token
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7); // Remove "Bearer " prefix
                        try {
                            String username = jwtTokenHelper.getUsernameFromToken(token);
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            
                            if (jwtTokenHelper.validateToken(token, userDetails)) {
                                // Create authentication object
                                UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
                                    );
                                // Set the authentication in the SecurityContext
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                accessor.setUser(authentication);
                                System.out.println("✅ WebSocket authenticated for user: " + username);
                            } else {
                                System.err.println("❌ Invalid token for WebSocket connection");
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Error validating WebSocket token: " + e.getMessage());
                        }
                    } else {
                        System.err.println("❌ No valid token found in WebSocket handshake");
                    }
                }
                return message;
            }
        });
    }
}