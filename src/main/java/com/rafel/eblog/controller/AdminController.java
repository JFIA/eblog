package com.rafel.eblog.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rafel.eblog.common.lang.Result;
import com.rafel.eblog.entity.Post;
import com.rafel.eblog.vo.PostVo;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {


    /**
     * @param id
     * @param rank  0表示取消，1表示操作
     * @param field
     * @return
     */
    @ResponseBody
    @PostMapping("/jie-set")
    public Result jieSet(@RequestParam Long id, @RequestParam Integer rank, @RequestParam String field) {

        Post post = postService.getById(id);

        Assert.isTrue(post != null, "该帖子已删除!");

        switch (field) {
            case "delete":
                postService.removeById(id);
                return Result.success();
            case "status":
                assert false;
                post.setRecommend(rank == 1);

                break;
            case "stick":
                assert false;
                post.setLevel(rank);
                break;
        }

        postService.updateById(post);

        return Result.success();
    }

    @ResponseBody
    @PostMapping("/initEsData")
    public Result initEsData() {

        // 页容量
        long size = 10000;
        Page page = new Page();
        page.setSize(size);

        long total = 0;

        for (int i = 1; i < 1000; i++) {
            // 当前页码
            page.setCurrent(i);

            IPage<PostVo> paging = postService.paging(page, null, null, null, null, null);
            int num = searchService.initEsData(paging.getRecords());

            total += num;

            // 当一页查不出10000条的时候，说明是最后一页了
            if(paging.getRecords().size() < size) {
                break;
            }
        }

        return Result.success("导入ES成功，共 " + total + " 条记录！", null);
    }

}
