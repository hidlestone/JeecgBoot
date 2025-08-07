
## sentinel
```
#Sentinel配置
sentinel:
  transport:
    dashboard: jeecg-boot-sentinel:9000
  # 支持链路限流
  web-context-unify: false
  filter:
    enabled: false
  # 取消Sentinel控制台懒加载
  eager: false
  datasource:
    #流控规则
    flow:  # 指定数据源名称
      # 指定nacos数据源
      nacos:
        server-addr: @config.server-addr@
        # 指定配置文件
        dataId: ${spring.application.name}-flow-rules
        # 指定分组
        groupId: SENTINEL_GROUP
        # 指定配置文件规则类型
        rule-type: flow
        # 指定配置文件数据格式
        data-type: json
    #降级规则
    degrade:
      nacos:
        server-addr: @config.server-addr@
        dataId: ${spring.application.name}-degrade-rules
        groupId: SENTINEL_GROUP
        rule-type: degrade
        data-type: json
```
jeecg-gateway-flow-rules 文件的内容可能是：
```
[
  {
    "resource": "/api/user/**",
    "count": 10,
    "grade": 1,
    "limitApp": "default",
    "strategy": 0,
    "controlBehavior": 0
  }
]
```
这个配置实现了 Sentinel 与 Nacos 的集成，使得：
- 流控规则可以动态配置
- 规则变更无需重启应用
- 支持多环境规则管理

这是微服务架构中常见的配置中心 + 限流熔断的组合方案。


## Skywalking
Skywalking 执行参数：
```
-javaagent:C:\SOFTWARE\skywalking\apache-skywalking-apm-8.6.0\apache-skywalking-apm-bin\agent\skywalking-agent.jar
-Dskywalking.agent.service_name=jeecg-gateway
-Dskywalking.collector.backend_service=127.0.0.1:11800
```

## RabbitMQ
`http://localhost:15672`



