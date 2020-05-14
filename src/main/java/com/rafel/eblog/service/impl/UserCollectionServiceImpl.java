package com.rafel.eblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rafel.eblog.entity.UserCollection;
import com.rafel.eblog.mapper.UserCollectionMapper;
import com.rafel.eblog.service.UserCollectionService;
import org.springframework.stereotype.Service;

@Service
public class UserCollectionServiceImpl extends ServiceImpl<UserCollectionMapper, UserCollection> implements UserCollectionService {
}
