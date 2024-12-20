package com.blog_app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blog_app.payloads.CommentDto;
import com.blog_app.payloads.ReplyDto;
import com.blog_app.services.ReplyService;

@RestController
@RequestMapping("/api")
public class ReplyController {

	@Autowired
	private ReplyService replyService;
	
	
	@PostMapping("/comment/{commentId}/reply")
    public ResponseEntity<ReplyDto> createReply(
            @RequestBody ReplyDto replyDto,
            @PathVariable Integer commentId,
            @RequestParam Integer userId) {
        ReplyDto createdReply = this.replyService.createReply(replyDto, userId, commentId);
        return ResponseEntity.ok(createdReply);
    }

    @GetMapping("/comment/{commentId}/replies")
    public ResponseEntity<List<ReplyDto>> getRepliesByComment(@PathVariable Integer commentId) {
        List<ReplyDto> replies = this.replyService.getRepliesByComment(commentId);
        return ResponseEntity.ok(replies);
    }

    @DeleteMapping("/reply/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Integer replyId) {
        this.replyService.deleteReply(replyId);
        return ResponseEntity.noContent().build();
    }
    
 // // Update Reply
    @PutMapping("/update/{replyId}")
    public ResponseEntity<ReplyDto> updateReply(
            @RequestBody ReplyDto replyDto,
            @PathVariable Integer replyId) {
        ReplyDto updatedReply = replyService.updateReply(replyDto, replyId);
        return ResponseEntity.ok(updatedReply);
    }
    }

