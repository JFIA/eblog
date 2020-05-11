package com.rafel.eblog.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rafel.eblog.service.CommentService;
import com.rafel.eblog.service.PostService;
import com.rafel.eblog.service.UserService;
import com.rafel.eblog.shiro.AccountProfile;
import org.apache.shiro.SecurityUtils;
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

    public Page getPage() {

        int pn = ServletRequestUtils.getIntParameter(req, "pn", 1);
        int size = ServletRequestUtils.getIntParameter(req, "size", 2);

        return new Page(pn, size);
    }

    public AccountProfile getProfile(){

        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

    public long getProfileById() {

        return getProfile().getId();
    }

}
