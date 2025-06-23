package com.blog_app.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.blog_app.Security.CustomUserDetailService;
import com.blog_app.Security.JwtAuthenticationEntryPoint;
import com.blog_app.Security.JwtAuthenticationFilter;
import com.blog_app.Security.JwtTokenHelper;
import static org.springframework.http.HttpMethod.OPTIONS;



import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import static org.springframework.http.HttpMethod.OPTIONS;


@Configuration
@EnableWebSecurity
@EnableWebMvc
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
	
	
	
	@Autowired
	private JwtTokenHelper jwtTokenHelper;
	//private final JwtAuthenticationFilter filter;
    //private final JwtAuthenticationEntryPoint point;
	
	@Autowired
	private CustomUserDetailService customUserDetailService;
	
	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	 
	public static final String[] PUBLIC_URLS = {
			"/api/v1/auth/login","/api/v1/auth/resend-otp","/ws/**","/api/v1/auth/edit-email","/api/ai/generateStream", "/api/v1/oauth2/**","/api/v1/oauth2/google/callback","/api/v1/auth/register","/api/v1/auth/verify-otp","/api/v1/auth/upload-profile-pic", "/v3/api-docs/**", "/v2/api-docs", "/swagger-resources/**", "/swagger-ui/**", "/webjars/**","/api/password/**","/api/post/view/**"
	};
	
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       // JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenHelper);

		http
        .addFilterBefore(jwtAuthenticationFilter, AnonymousAuthenticationFilter.class) 
        .csrf(csrf -> csrf.disable())	
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Add CORS configuration
        .authorizeHttpRequests((authz) -> authz
            .requestMatchers(PUBLIC_URLS).permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 👈 Allow OPTIONS
            //.requestMatchers("/ws/**").authenticated()// added dis for authentication the websocket connection
            .anyRequest().authenticated())
        .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		
		 // OAuth2 Login Configuration
		.oauth2Login(oauth2 -> oauth2
			    .loginPage("/api/v1/oauth2/google/callback") // Customize callback URL
			    .authorizationEndpoint(authorization -> authorization
			        .baseUri("/oauth2/authorization/google") // Customize authorization endpoint
			    )
			    .tokenEndpoint(token -> token
			        .accessTokenResponseClient(accessTokenResponseClient()) // Customize token response client
			    )
			);
        
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
		
	}
	
	@Bean
	public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
	    return new DefaultAuthorizationCodeTokenResponseClient();
	}
	
    @Bean
    public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		
		auth.userDetailsService(this.customUserDetailService).passwordEncoder(passwordEncoder());
		
	}
	
	@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
	
//	@Bean
//	public FilterRegistrationBean<org.springframework.web.filter.CorsFilter> coresFilter() {
//	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//	    CorsConfiguration corsConfiguration = new CorsConfiguration();
//
//	    corsConfiguration.setAllowCredentials(true);
////	    corsConfiguration.addAllowedOriginPattern("http://localhost:9090");  // Allow localhost:9090
//	    corsConfiguration.addAllowedOriginPattern("http://localhost:3000");
////	    corsConfiguration.addAllowedOrigin("http://localhost");  // Frontend URL
////	    corsConfiguration.addAllowedOrigin("http://localhost:80"); 
//	    corsConfiguration.addAllowedHeader("Authorization");
//	    corsConfiguration.addAllowedHeader("Content-Type");
//	    corsConfiguration.addAllowedHeader("Accept");
//	    corsConfiguration.addAllowedMethod("GET");
//	    corsConfiguration.addAllowedMethod("POST");
//	    corsConfiguration.addAllowedMethod("PUT");
//	    corsConfiguration.addAllowedMethod("DELETE");
//	    corsConfiguration.addAllowedMethod("OPTIONS");
//	    corsConfiguration.setMaxAge(3600L);
//
//	    source.registerCorsConfiguration("/**", corsConfiguration);
//
//	    // Explicitly pass CorsFilter class in the FilterRegistrationBean constructor
//	    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean(new CorsFilter(source));
//	    bean.setOrder(0);
//	    return bean;
//	}
//	@Bean
//	public CorsConfigurationSource corsConfigurationSource() {
//	    CorsConfiguration configuration = new CorsConfiguration();
//	    // Use allowedOriginPatterns instead of allowedOrigins
//	    configuration.setAllowedOriginPatterns(List.of("http://localhost:3000")); // Explicitly list allowed origins
//	    configuration.addAllowedMethod("*"); // Allow all HTTP methods
//	    configuration.addAllowedHeader("*"); // Allow all headers
//	    configuration.setAllowCredentials(true); // Allow credentials
//
//	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//	    source.registerCorsConfiguration("/**", configuration); // Apply CORS configuration to all paths
//	    return source;
//	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowedOriginPatterns(List.of("http://localhost:3000", "https://bewbew.serveblog.net"));
	    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); 
	    config.setAllowedHeaders(List.of("Authorization", "Content-Type")); 
	    config.setAllowCredentials(true);
	    config.setMaxAge(3600L);

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);
	    return source;
	}
	 
	 @Bean
	    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
	        StrictHttpFirewall firewall = new StrictHttpFirewall();
	        firewall.setAllowUrlEncodedSlash(true);  // Allow encoded slashes
	        firewall.setAllowSemicolon(true);       // Allow semicolons (optional, based on your needs)
	        firewall.setAllowUrlEncodedDoubleSlash(true);     // Allow double slashes in URLs
	        return firewall;
	    }
}

