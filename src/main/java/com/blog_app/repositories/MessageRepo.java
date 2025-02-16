package com.blog_app.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.blog_app.entities.Message;
import com.blog_app.entities.User;

public interface MessageRepo extends JpaRepository<Message, Integer> {
    List<Message> findBySenderAndReceiverOrReceiverAndSender(User sender1, User receiver1, User sender2, User receiver2);

    List<Message> findDistinctBySenderOrReceiver(User sender, User receiver);
    
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) " +
            "OR (m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.sentAt DESC")
     Page<Message> findChatHistory(@Param("user1") User user1, 
                                   @Param("user2") User user2, 
                                   Pageable pageable);
    
//    @Query("SELECT m FROM Message m WHERE " +
//    	       "(m.sender = :user1 AND m.receiver = :user2) " +
//    	       "OR (m.sender = :user2 AND m.receiver = :user1) " +
//    	       "ORDER BY m.sentAt ASC")
//    	Page<Message> findChatHistory(@Param("user1") User user1, 
//    	                              @Param("user2") User user2, 
//    	                              Pageable pageable);

}
