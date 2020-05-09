package com.rafel.eblog.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rafel.eblog.entity.Post;
import com.rafel.eblog.vo.CommentVo;
import com.rafel.eblog.vo.PostVo;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PostController extends BaseController {

    // 指定接收参数是数值类型
    @GetMapping("/category/{id:\\d*}")
    public String category(@PathVariable(name = "id") long id) {

        int pn = ServletRequestUtils.getIntParameter(req, "pn", 1);

        req.setAttribute("currentCategoryId", id);
        req.setAttribute("pn", pn);

        return "post/category";
    }

    @GetMapping("/post/{id:\\d*}")
    public String detail(@PathVariable(name = "id") long id) {

        PostVo postVo = postService.selectOnePost(new QueryWrapper<Post>()
                .eq("p.id", id));
        Assert.notNull(postVo, "文章已被删除");

        postService.putViewCount(postVo);

        // 1分页，2文章id，3用户id，排序
        IPage<CommentVo> results = commentService.paging(getPage(), postVo.getId(), null, "created");

        req.setAttribute("currentCategoryId", postVo.getCategoryId());
        req.setAttribute("post", postVo);


        return "post/detail";
    }

}
