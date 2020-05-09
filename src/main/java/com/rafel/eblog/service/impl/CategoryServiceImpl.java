package com.rafel.eblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rafel.eblog.entity.Category;
import com.rafel.eblog.mapper.CategoryMapper;
import com.rafel.eblog.service.CategoryService;
import org.springframework.stereotype.Service;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


}
