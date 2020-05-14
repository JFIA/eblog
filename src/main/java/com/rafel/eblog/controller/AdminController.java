package com.rafel.eblog.controller;


import com.rafel.eblog.common.lang.Result;
import com.rafel.eblog.entity.Post;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AdminController extends BaseController {

    @ResponseBody
    @PostMapping("/jie-set")
    public Result jieSet(Long id, Integer rank, String field) {
        Post post = postService.getById(id);

        Assert.isTrue(post == null, "该帖子已删除!");

        switch (field) {
            case "delete":
                postService.removeById(id);
                return Result.success();
            case "status":
                post.setRecommend(rank == 1);

                break;
            case "stick":
                post.setLevel(rank);
                break;
        }

        postService.updateById(post);

        return Result.success();
    }

}
