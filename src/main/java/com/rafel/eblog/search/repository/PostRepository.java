package com.rafel.eblog.search.repository;


import com.rafel.eblog.search.model.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


// 符合jpa命名规范的接口
@Repository
public interface PostRepository extends ElasticsearchRepository<PostDocument, Long> {
}
