package com.rafel.eblog.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rafel.eblog.entity.Post;
import com.rafel.eblog.search.model.PostDocument;
import com.rafel.eblog.search.mq.PostMqIndexMessage;
import com.rafel.eblog.search.repository.PostRepository;
import com.rafel.eblog.service.PostService;
import com.rafel.eblog.service.SearchService;
import com.rafel.eblog.vo.PostVo;
import javafx.geometry.Pos;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    PostRepository postRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PostService postService;

    @Override
    public IPage search(Page page, String keyword) {
        // 分页信息 mybatis plus的page 转成 jpa的page
        long current = page.getCurrent() - 1;
        long size = page.getSize();

        Pageable pageable = PageRequest.of((int) current, (int) size);

        // 搜索es得到pageData
        MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(keyword, "title", "authorName", "categoryName");

        org.springframework.data.domain.Page<PostDocument> document = postRepository.search(queryBuilder, pageable);

        // 结果信息 jpa的pageData转成mybatis plus的pageData
        IPage pages = new Page(page.getCurrent(), page.getSize(), document.getTotalElements());

        pages.setRecords(document.getContent());

        return pages;
    }

    @Override
    public int initEsData(List<PostVo> records) {

        if (records == null || records.size() == 0) return 0;

        List<PostDocument> list = new ArrayList<>();

        for (PostVo postVo : records) {

            // 映射转换
            PostDocument postDocument = modelMapper.map(postVo, PostDocument.class);
            list.add(postDocument);
        }


        postRepository.saveAll(list);

        log.info("es 初始化完成!");

        return list.size();
    }

    @Override
    public void createOrUpdateIndex(PostMqIndexMessage message) {
        Long id = message.getPostId();

        PostVo postVo = postService.selectOnePost(new QueryWrapper<Post>().eq("p.id", id));

        PostDocument postDocument = modelMapper.map(postVo, PostDocument.class);

        postRepository.save(postDocument);
        log.info("es 索引更新成功！ ---> {}", postDocument.toString());

    }

    @Override
    public void removeIndex(PostMqIndexMessage message) {

        Long id = message.getPostId();
        postRepository.deleteById(id);

        log.info("es 索引删除成功！ ---> {}", message.toString());

    }
}
