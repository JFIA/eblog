# eblog

#### 技术选型：

* 核心框架：Springboot 2.1.2
* 安全框架：Apache Shiro 1.4
* 持久层框架：Mybatis + mybatis plus 3.2.0
* 页面模板：Freemarker
* 缓存框架：Redis
* 数据库：mysql
* 消息队列：RabbitMq
* 分布式搜索：Elasticsearch 6.4.3
* 双工通讯协议：websocket
* 工具集合：hutool 4.1.17

#### 技术细节：
* 基于mybatis plus快速代码生成
* 使用shiro+redis完成了会话共享
* redis的zset结构完成本周热议排行榜
* t-io+websocket完成即时消息通知和群聊
* 基于rabbitmq+elasticsearch的内容同步与搜索引擎
