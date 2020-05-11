package com.rafel.eblog.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rafel.eblog.common.lang.Result;
import com.rafel.eblog.entity.Post;
import com.rafel.eblog.entity.User;
import com.rafel.eblog.service.UserService;
import com.rafel.eblog.util.UploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class UserController extends BaseController {

    @Autowired
    UploadUtil uploadUtil;

    @GetMapping("/user/home")
    public String home() {

        User user = userService.getById(getProfileById());

        List<Post> posts = postService.list(new QueryWrapper<Post>()
                .eq("user_id", getProfileById())
                .orderByDesc("created")
        );

        req.setAttribute("user", user);
        req.setAttribute("posts", posts);

        return "/user/home";
    }

    @GetMapping("/user/set")
    public String set(){
        User user = userService.getById(getProfileById());

        req.setAttribute("user", user);

        return "/user/set";
    }

    @ResponseBody
    @PostMapping("/user/set")
    public Result doSet(User user) {
        Result result = userService.updateUser(user);

        return result.action("/user/set#info");
    }

    @ResponseBody
    @PostMapping("/user/upload")
    public Result upload(@RequestParam(value = "file")MultipartFile multipartFile) throws IOException {

        return uploadUtil.upload(UploadUtil.type_avatar, multipartFile);
    }

    @ResponseBody
    @PostMapping("/user/repass")
    public Result repass(String nowpass, String pass, String repass) {
        if(!pass.equals(repass)) {
            return Result.fail("两次密码不相同");
        }

        User user = userService.getById(getProfileById());

        String nowPassMd5 = SecureUtil.md5(nowpass);
        if(!nowPassMd5.equals(user.getPassword())) {
            return Result.fail("密码不正确");
        }

        user.setPassword(SecureUtil.md5(pass));
        userService.updateById(user);

        return Result.success().action("/user/set#pass");

    }

}
