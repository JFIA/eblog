package com.rafel.eblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rafel.eblog.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rafel.eblog.vo.CommentVo;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface CommentService extends IService<Comment> {

    IPage<CommentVo> paging(Page page, Long postId, Long userId, String order);
}
