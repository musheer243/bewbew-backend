package com.blog_app.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@ManyToOne
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;
	
	@ManyToOne
	@JoinColumn(name = "reciever_id", nullable = false)
	private User receiver;
	
	 	@Column(nullable = false, length = 1000)
	    private String content;

	    @Column(name = "is_read", nullable = false)
	    private boolean isRead = false;

	    @Column(name = "sent_at", nullable = false)
	    private LocalDateTime sentAt = LocalDateTime.now();
	    
	 // Tracks which users have deleted the message
	    @ElementCollection(fetch = FetchType.LAZY)
	    @CollectionTable(name = "deleted_messages", joinColumns = @JoinColumn(name = "message_id"))
	    private Set<Integer> deletedFor = new HashSet<>();
}
