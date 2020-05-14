package com.rafel.eblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rafel.eblog.entity.UserMessage;
import com.rafel.eblog.service.UserMessageService;
import com.rafel.eblog.service.WsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class WsServiceImpl implements WsService {

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    @Async
    public void sendMessCountToUser(Long toUserId) {

        int count = userMessageService.count(new QueryWrapper<UserMessage>()
                .eq("to_user_id", toUserId)
                .eq("status", 0));

        // websocket异步通知 (/user/20/messCount)
        simpMessagingTemplate.convertAndSendToUser(toUserId.toString(), "/messCount", count);

    }
}
