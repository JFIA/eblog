package com.rafel.eblog.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rafel.eblog.common.lang.Result;
import com.rafel.eblog.entity.Post;
import com.rafel.eblog.entity.User;
import com.rafel.eblog.entity.UserMessage;
import com.rafel.eblog.service.UserService;
import com.rafel.eblog.util.UploadUtil;
import com.rafel.eblog.vo.UserMessageVo;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class UserController extends BaseController {

    @Autowired
    UploadUtil uploadUtil;

    @GetMapping({"/user/home", "/user/{id:\\d*}"})
    public String home(@PathVariable Long id) {

        User user;
        if (id != null) {
            user = userService.getById(id);
        } else user = userService.getById(getProfileById());

        List<Post> posts = postService.list(new QueryWrapper<Post>()
                .eq("user_id", getProfileById())
                .orderByDesc("created")
        );

        req.setAttribute("user", user);
        req.setAttribute("posts", posts);

        return "/user/home";
    }

    @GetMapping("/user/set")
    public String set() {
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
    public Result upload(@RequestParam(value = "file") MultipartFile multipartFile) throws IOException {

        return uploadUtil.upload(UploadUtil.type_avatar, multipartFile);
    }

    @ResponseBody
    @PostMapping("/user/repass")
    public Result repass(String nowpass, String pass, String repass) {
        if (!pass.equals(repass)) {
            return Result.fail("两次密码不相同");
        }

        User user = userService.getById(getProfileById());

        String nowPassMd5 = SecureUtil.md5(nowpass);
        if (!nowPassMd5.equals(user.getPassword())) {
            return Result.fail("密码不正确");
        }

        user.setPassword(SecureUtil.md5(pass));
        userService.updateById(user);

        return Result.success().action("/user/set#pass");

    }

    @GetMapping("/user/index")
    public String index() {
        return "/user/index";
    }

    @ResponseBody
    @GetMapping("/user/public")
    public Result userP() {

        IPage page = postService.page(getPage(), new QueryWrapper<Post>()
                .eq("user_id", getProfileById())
                .orderByDesc("created"));

        return Result.success(page);

    }

    @ResponseBody
    @GetMapping("/user/collection")
    public Result collection() {

        IPage page = postService.page(getPage(), new QueryWrapper<Post>()
                .inSql("id", "SELECT post_id FROM m_user_collection where user_id = " + getProfileById()));

        return Result.success(page);
    }

    @GetMapping("/user/mess")
    public String message() {

        IPage<UserMessageVo> pages = messageService.paging(getPage(), new QueryWrapper<UserMessage>()
                .eq("to_user_id", getProfileById())
                .orderByDesc("created"));

        // 把消息改成已读状态
        List<Long> ids = new ArrayList<>();
        for (UserMessageVo messageVo : pages.getRecords()) {
            if (messageVo.getStatus() == 0) {
                ids.add(messageVo.getId());
            }
        }

        // 批量修改成已读
        messageService.updateToRead(ids);

        req.setAttribute("pageData", pages);
        return "/user/mess";
    }

    @ResponseBody
    @GetMapping("/msg/remove/")
    public Result remove(long id, @RequestParam(defaultValue = "false") boolean all) {

        boolean remove = messageService.remove(new QueryWrapper<UserMessage>()
                .eq("to_user_id", getProfileById())
                .eq(!all, "id", id));
        return remove ? Result.success() : Result.fail("删除失败!");
    }

    @ResponseBody
    @RequestMapping("/message/nums/")
    public Map msgNums() {

        int count = messageService.count(new QueryWrapper<UserMessage>()
                .eq("to_user_id", getProfileById())
                .eq("status", 0));

        return MapUtil.builder("status", 0)
                .put("count", count).build();
    }

}
