package com.rafel.eblog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rafel.eblog.common.lang.Result;
import com.rafel.eblog.controller.BaseController;
import com.rafel.eblog.entity.User;
import com.rafel.eblog.mapper.UserMapper;
import com.rafel.eblog.service.UserService;
import com.rafel.eblog.shiro.AccountProfile;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 服务实现类
 * </p>
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    BaseController baseController;

    @Override
    public Result register(User user) {
        int count = this.count(new QueryWrapper<User>()
                .eq("email", user.getEmail())
                .or()
                .eq("username", user.getUsername()));

        if (count > 0) return Result.fail("用户名或邮箱已被注册!");

        User user1 = new User();
        user1.setPassword(SecureUtil.md5(user.getPassword()));
        user1.setEmail(user.getEmail());
        user1.setUsername(user.getUsername());
        user1.setAvatar("/res/images/avatar/default.png");
        user1.setCreated(new Date());
        user1.setPoint(0);
        user1.setVipLevel(0);
        user1.setCommentCount(0);
        user1.setPostCount(0);
        user1.setGender("0");
        this.save(user1);

        return Result.success();
    }

    @Override
    public AccountProfile login(String email, String password) {

        User user = this.getOne(new QueryWrapper<User>()
                .eq("email", email));

        if (user == null) throw new UnknownAccountException();

        if (!user.getPassword().equals(password)) throw new IncorrectCredentialsException();

        user.setLasted(new Date());
        this.updateById(user);

        AccountProfile accountProfile = new AccountProfile();
        BeanUtil.copyProperties(user, accountProfile);

        return accountProfile;
    }

    @Override
    public Result updateUser(User user) {

        if (StrUtil.isBlank(user.getUsername())) return Result.fail("昵称不能为空!");

        if (StrUtil.isNotBlank(user.getAvatar())) {

            User temp = this.getById(baseController.getProfileById());
            temp.setAvatar(user.getAvatar());
            this.updateById(temp);

            AccountProfile profile = baseController.getProfile();
            profile.setAvatar(user.getAvatar());

            SecurityUtils.getSubject().getSession().setAttribute("profile", profile);

            return Result.success().action("/user/set#avatar");
        }

        int count = this.count(new QueryWrapper<User>()
                .eq("username", baseController.getProfile().getUsername())
                .ne("id", baseController.getProfileById()));
        if (count > 0) {
            return Result.fail("改昵称已被占用");
        }

        User temp = this.getById(baseController.getProfileById());
        temp.setUsername(user.getUsername());
        temp.setGender(user.getGender());
        temp.setSign(user.getSign());
        this.updateById(temp);

        // 更新shiro中登录的user对象
        AccountProfile profile = baseController.getProfile();
        profile.setUsername(temp.getUsername());
        profile.setSign(temp.getSign());
        SecurityUtils.getSubject().getSession().setAttribute("profile", profile);

        return Result.success();
    }
}
