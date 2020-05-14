package com.rafel.eblog.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rafel.eblog.common.lang.Result;
import com.rafel.eblog.entity.*;
import com.rafel.eblog.util.ValidationUtil;
import com.rafel.eblog.vo.CommentVo;
import com.rafel.eblog.vo.PostVo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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
        req.setAttribute("pageData", results);

        return "post/detail";
    }

    @ResponseBody
    @PostMapping("/collection/find/")
    public Result collectionFind(@RequestParam long pid) {

        int count = collectionService.count(new QueryWrapper<UserCollection>()
                .eq("user_id", getProfileById())
                .eq("post_id", pid));

        return Result.success(MapUtil.of("collection", count > 0));
    }

    @ResponseBody
    @PostMapping("/collection/add/")
    public Result collectionAdd(@RequestParam long pid) {
        Post post = postService.getById(pid);

        // 如果为空，通过，否则发出message
        Assert.isTrue(post != null, "帖子已被删除!");

        int count = collectionService.count(new QueryWrapper<UserCollection>()
                .eq("user_id", getProfileById())
                .eq("post_id", pid));
        if (count > 0) return Result.fail("已被收藏!");

        UserCollection userCollection = new UserCollection();

        userCollection.setPostId(pid);
        userCollection.setUserId(getProfileById());
        userCollection.setCreated(new Date());
        userCollection.setPostUserId(post.getUserId());
        userCollection.setModified(new Date());

        collectionService.save(userCollection);

        return Result.success();
    }

    @ResponseBody
    @PostMapping("/collection/remove/")
    public Result collectionRemove(@RequestParam long pid) {

        Post post = postService.getById(pid);
        Assert.isTrue(post != null, "改帖子已被删除");

        collectionService.remove(new QueryWrapper<UserCollection>()
                .eq("user_id", getProfileById())
                .eq("post_id", pid));

        return Result.success();
    }

    @GetMapping("/post/edit")
    public String edit() {
        String id = req.getParameter("id");

        if (!StrUtil.isBlank(id)) {
            Post post = postService.getById(id);
            Assert.isTrue(post != null, "帖子已被删除!");
            Assert.isTrue(post.getUserId().longValue() == getProfileById().longValue(), "没权限操作此文章!");
            req.setAttribute("post", post);

        }

        req.setAttribute("categories", categoryService.list());

        return "/post/edit";
    }

    @PostMapping("/post/submit")
    public Result submit(Post post) {
        ValidationUtil.ValidResult validResult = ValidationUtil.validateBean(post);
        if (validResult.hasErrors()) {
            return Result.fail(validResult.getErrors());
        }

        // id为空，说明数据库中不存在此条记录，初始化对象并保存
        if (post.getId() == null) {
            post.setUserId(getProfileById());

            post.setModified(new Date());
            post.setCreated(new Date());
            post.setCommentCount(0);
            post.setEditMode(null);
            post.setLevel(0);
            post.setRecommend(false);
            post.setViewCount(0);
            post.setVoteDown(0);
            post.setVoteUp(0);
            postService.save(post);

        } else {
            Post tempPost = postService.getById(post.getId());
            Assert.isTrue(tempPost.getUserId().longValue() == getProfileById().longValue(), "无权限编辑此文章！");

            tempPost.setTitle(post.getTitle());
            tempPost.setContent(post.getContent());
            tempPost.setCategoryId(post.getCategoryId());
            postService.updateById(tempPost);
        }

        return Result.success().action("/post/" + post.getId());
    }

    @ResponseBody
    @Transactional
    @PostMapping("/post/delete")
    public Result delete(Long id) {
        Post post = postService.getById(id);

        Assert.notNull(post, "该帖子已被删除");
        Assert.isTrue(post.getUserId().longValue() == getProfileById().longValue(), "无权限删除此文章！");

        postService.removeById(id);

        // 删除相关消息、收藏等
        messageService.removeByMap(MapUtil.of("post_id", id));
        collectionService.removeByMap(MapUtil.of("post_id", id));

//        amqpTemplate.convertAndSend(RabbitConfig.es_exchage, RabbitConfig.es_bind_key,
//                new PostMqIndexMessage(post.getId(), PostMqIndexMessage.REMOVE));

        return Result.success().action("/user/index");
    }

    @ResponseBody
    @Transactional
    @PostMapping("/post/reply/")
    public Result reply(Long jid, String content) {
        Assert.notNull(jid, "找不到对应的文章!");
        Assert.notNull(content, "评论的内容不能为空!");

        Post post = postService.getById(jid);
        Assert.isTrue(post != null, "该文章已被删除!");

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPostId(jid);
        comment.setUserId(getProfileById());
        comment.setCreated(new Date());
        comment.setModified(new Date());
        comment.setLevel(0);
        comment.setVoteDown(0);
        comment.setVoteUp(0);
        commentService.save(comment);

        // 评论数量加一
        post.setCommentCount(post.getCommentCount() + 1);
        postService.updateById(post);

        // 本周热议数量加一
        postService.incrCommentCountAndUnionForWeekRank(post.getId(), true);

        // 通知作者，有人评论了你的文章
        // 作者自己评论自己文章，不需要通知
        if(comment.getUserId() != post.getUserId()) {
            UserMessage message = new UserMessage();
            message.setPostId(jid);
            message.setCommentId(comment.getId());
            message.setFromUserId(getProfileById());
            message.setToUserId(post.getUserId());
            message.setType(1);
            message.setContent(content);
            message.setCreated(new Date());
            message.setStatus(0);
            messageService.save(message);

            // 即时通知作者（websocket）,不需要手动刷新
            wsService.sendMessCountToUser(message.getToUserId());
        }

        // 通知被@的人，有人回复了你的文章
        if(content.startsWith("@")){
            String username=content.substring(1, content.indexOf(" "));

            User user = userService.getOne(new QueryWrapper<User>()
                    .eq("username", username));

            if (user!=null){
                UserMessage message = new UserMessage();
                message.setPostId(jid);
                message.setCommentId(comment.getId());
                message.setFromUserId(getProfileById());
                message.setToUserId(user.getId());
                message.setType(2);
                message.setContent(content);
                message.setCreated(new Date());
                message.setStatus(0);
                messageService.save(message);

                // 即时通知被@的用户
                wsService.sendMessCountToUser(message.getToUserId());
            }
        }

        return Result.success().action("/post/" + post.getId());
    }

    @ResponseBody
    @Transactional
    @PostMapping("/post/jieda-delete/")
    public Result removeComment(Long id) {

        Assert.notNull(id, "评论id不能为空！");

        Comment comment = commentService.getById(id);

        Assert.notNull(comment, "找不到对应评论！");

        if(comment.getUserId().longValue() != getProfileById().longValue()) {
            return Result.fail("必须删除自己的评论！");
        }
        commentService.removeById(id);

        // 评论数量减一
        Post post = postService.getById(comment.getPostId());
        post.setCommentCount(post.getCommentCount() - 1);
        postService.saveOrUpdate(post);

        //评论数量减一
        postService.incrCommentCountAndUnionForWeekRank(comment.getPostId(), false);

        return Result.success();
    }


}
