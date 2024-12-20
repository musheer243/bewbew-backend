package com.blog_app.services;

import java.util.List;

import com.blog_app.payloads.ReplyDto;

public interface ReplyService {

	ReplyDto createReply(ReplyDto replyDto, Integer userId, Integer commentId);

    void deleteReply(Integer replyId);

    ReplyDto updateReply(ReplyDto replyDto, Integer replyId);

    List<ReplyDto> getRepliesByComment(Integer commentId);

}
