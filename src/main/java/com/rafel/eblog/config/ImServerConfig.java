package com.rafel.eblog.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImServerConfig {

    @Value("${im.server.port}")
    private int imPort;

    // 启动tio服务

    // 初始化消息处理器类型
}
