package com.rafel.eblog.shiro;

import cn.hutool.json.JSONUtil;
import com.rafel.eblog.common.lang.Result;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authc.UserFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AuthFilter extends UserFilter {

    @Override
    protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        String head = httpServletRequest.getHeader("X-Requested-With");
        boolean authenticated = SecurityUtils.getSubject().isAuthenticated();

        if (!authenticated && head.equals("XMLHttpRequest")) {

            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(JSONUtil.toJsonStr(Result.fail("请先登录！")));

        } else super.redirectToLogin(request, response);
    }
}
