package com.rafel.eblog.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.google.code.kaptcha.Producer;
import com.rafel.eblog.common.lang.Result;
import com.rafel.eblog.entity.User;
import com.rafel.eblog.util.ValidationUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Controller
public class AuthController extends BaseController {

    private static final String KAPTCHA_SESSION_KEY = "KAPTCHA_SESSION_KEY";

    @Autowired
    Producer producer;

    @GetMapping("/capthca.jpg")
    public void kaptcha(HttpServletResponse resp) throws IOException {

        // 验证码
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        req.getSession().setAttribute(KAPTCHA_SESSION_KEY, text);

        resp.setHeader("Cache-Control", "no-store, no-cache");
        resp.setContentType("image/jpeg");
        ServletOutputStream outputStream = resp.getOutputStream();
        ImageIO.write(image, "jpg", outputStream);
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "reg";
    }

    @ResponseBody
    @PostMapping("/login")
    public Result doLogin(@RequestParam String email, @RequestParam String password) {
        if (StrUtil.isBlank(email) || StrUtil.isBlank(password)) return Result.fail("邮箱或密码不能为空!");

        UsernamePasswordToken usernamePasswordToken=new UsernamePasswordToken(email, SecureUtil.md5(password));

        try {
            SecurityUtils.getSubject().login(usernamePasswordToken);
        }catch (AuthenticationException exception){
            if (exception instanceof UnknownAccountException) {
                return Result.fail("用户不存在");
            } else if (exception instanceof LockedAccountException) {
                return Result.fail("用户被禁用");
            } else if (exception instanceof IncorrectCredentialsException) {
                return Result.fail("密码错误");
            } else {
                return Result.fail("用户认证失败");
            }
        }

        return Result.success().action("/");
    }

    @ResponseBody
    @PostMapping("/register")
    public Result doRegister(User user, String repass, String verCode) {
        ValidationUtil.ValidResult validResult = ValidationUtil.validateBean(user);

        if (validResult.hasErrors()) {
            return Result.fail(validResult.getErrors());
        }

        if (!user.getPassword().equals(repass)) {
            return Result.fail("密码不一致!");
        }

        String kaptcha_session_key = (String) req.getSession().getAttribute("KAPTCHA_SESSION_KEY");

        if (verCode == null || !kaptcha_session_key.equalsIgnoreCase(KAPTCHA_SESSION_KEY)) {
            return Result.fail("验证码错误!");
        }

        // 完成注册，更新数据库
        Result result = userService.register(user);

        return result.action("/login");

    }

    @RequestMapping("/user/logout")
    public String logout() {
        SecurityUtils.getSubject().logout();
        return "redirect:/";
    }

}
