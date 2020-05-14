package com.rafel.eblog.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rafel.eblog.entity.UserMessage;
import com.rafel.eblog.mapper.UserMessageMapper;
import com.rafel.eblog.service.UserMessageService;
import com.rafel.eblog.vo.UserMessageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserMessageServiceImpl extends ServiceImpl<UserMessageMapper, UserMessage> implements UserMessageService {

    @Autowired
    UserMessageMapper messageMapper;

    @Override
    public IPage paging(Page page, QueryWrapper<UserMessage> wrapper) {

        IPage<UserMessageVo> pages = messageMapper.selectMessages(page, wrapper);

        return pages;
    }

    @Override
    public void updateToRead(List<Long> ids) {
        if(ids.isEmpty()) return;

        messageMapper.updateToRead(new QueryWrapper<UserMessage>()
                .in("id", ids)
        );
    }
}
