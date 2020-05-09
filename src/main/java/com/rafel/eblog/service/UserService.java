package com.rafel.eblog.service;

import com.rafel.eblog.common.lang.Result;
import com.rafel.eblog.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rafel.eblog.shiro.AccountProfile;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface UserService extends IService<User> {

    Result register(User user);

    AccountProfile login(String username, String password);
}
