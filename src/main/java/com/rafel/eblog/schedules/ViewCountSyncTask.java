package com.rafel.eblog.schedules;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rafel.eblog.entity.Post;
import com.rafel.eblog.service.PostService;
import com.rafel.eblog.util.RedisUtil;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ViewCountSyncTask {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    PostService postService;

    // 5s更新一次
    @Scheduled(cron = "0/5 * * * * *")
    public void task() {

        Set<String> keys = redisTemplate.keys("rank:post:*");

        // 列表中是需要更新阅读量的postId
        ArrayList<String> ids = new ArrayList<>();

        for (String key : keys) {
            if (redisUtil.hHasKey(key, "post:viewCount")) {

                ids.add(key.substring("rank:post:".length()));
            }

        }

        if (ids.isEmpty()) return;

        // 更新阅读量
        List<Post> posts = postService.list(new QueryWrapper<Post>().in("id", ids));
        for (Post post : posts) {

            Integer viewCount = (Integer) redisUtil.hget("rank:post:" + post.getId(), "post:viewCount");

            post.setViewCount(viewCount);

        }

        if (posts.isEmpty()) return;

        boolean status = postService.updateBatchById(posts);

        if (status) {
            ids.forEach((id) -> {
                redisUtil.hdel("rank:post:" + id, "post:viewCount");
                System.out.println(id + "---------------------->同步成功");
            });

        }


    }

}
