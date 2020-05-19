package com.rafel.eblog.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rafel.eblog.mapper.UserCollectionMapper;
import com.rafel.eblog.mapper.UserMessageMapper;
import com.rafel.eblog.service.*;
import com.rafel.eblog.shiro.AccountProfile;
import org.apache.shiro.SecurityUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;

import static com.baomidou.mybatisplus.core.toolkit.IdWorker.getId;

@Controller
public class BaseController {

    @Autowired
    HttpServletRequest req;

    @Autowired
    PostService postService;

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;

    @Autowired
    UserMessageService messageService;

    @Autowired
    UserCollectionService collectionService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WsService wsService;

    @Autowired
    SearchService searchService;

    @Autowired
    AmqpTemplate amqpTemplate;

    public Page getPage() {

        int pn = ServletRequestUtils.getIntParameter(req, "pn", 1);
        int size = ServletRequestUtils.getIntParameter(req, "size", 2);

        return new Page(pn, size);
    }

    public AccountProfile getProfile(){

        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

    public Long getProfileById() {

        return getProfile().getId();
    }

}
