package com.blog_app.Security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private JwtTokenHelper jwtTokenHelper;
	
	/*public JwtAuthenticationFilter(JwtTokenHelper jwtTokenHelper) {
        this.jwtTokenHelper = jwtTokenHelper;
    }*/
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
//		 // Skip token validation for streaming endpoints after the initial request
//	    if (request.getRequestURI().startsWith("/api/ai/generateStream") && !isInitialRequest(request)) {
//	        filterChain.doFilter(request, response);
//	        return;
//	    }
		 
		String requestToken = request.getHeader("Authorization");
		 
        //Bearer 2352345235sdfrsfgsdfsdf
		
		System.out.println("Authorization Header: " + requestToken);
		
		String username = null;
        String token = null;
        
        if (requestToken != null && requestToken.startsWith("Bearer ")) {
            //looking good 
           token = requestToken.substring(7);
            try {

                username = this.jwtTokenHelper.getUsernameFromToken(token);

            } catch (IllegalArgumentException e) {
                System.out.println("unable to get jwt token");
            } catch (ExpiredJwtException e) {
                System.out.println("jwt token is expired !!");
            } catch (MalformedJwtException e) {
                System.out.println("Invalid Token !!");
            }/* catch (Exception e) {
                e.printStackTrace();

            }*/


        } else {
        System.out.println("jwt token does not begin with Bearer");
        }


        
        
        //
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {


            //fetch user detail from username
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            
            if (this.jwtTokenHelper.validateToken(token, userDetails)) {

                //set the authentication
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);


            } else {
            System.out.println("invalid jwt token !!! ");
            }
            



        }
        else {
			System.out.println("username is null or context is not null");
		}

        filterChain.doFilter(request, response);


	}
	
//	private boolean isInitialRequest(HttpServletRequest request) {
//	    // Check if this is the initial request (not a chunk of a streamed response)
//	    return request.getHeader("Authorization") != null;
//	}

}

