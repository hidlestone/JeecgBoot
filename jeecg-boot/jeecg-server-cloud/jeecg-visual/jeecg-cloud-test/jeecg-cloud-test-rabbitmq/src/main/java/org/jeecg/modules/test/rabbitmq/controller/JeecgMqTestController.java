package org.jeecg.modules.test.rabbitmq.controller;

import cn.hutool.core.util.RandomUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.boot.starter.rabbitmq.client.RabbitMqClient;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.base.BaseMap;
import org.jeecg.modules.test.rabbitmq.constant.CloudConstant;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * RabbitMqClient发送消息
 *
 * @author: zyf
 * @date: 2022/04/21
 */
@RestController
@RequestMapping("/sys/test")
@Tag(name = "【微服务】MQ单元测试")
public class JeecgMqTestController {

    @Autowired
    private RabbitMqClient rabbitMqClient;


    /**
     * 测试方法：快速点击发送MQ消息
     * 观察三个接受者如何分配处理消息：HelloReceiver1、HelloReceiver2、HelloReceiver3，会均衡分配
     *
     * @param req
     * @return
     */
    @GetMapping(value = "/rabbitmq")
    @Operation(summary = "测试rabbitmq")
    public Result<?> rabbitMqClientTest(HttpServletRequest req) {
        //rabbitmq消息队列测试
        BaseMap map = new BaseMap();
        map.put("orderId", RandomUtil.randomNumbers(10));
        rabbitMqClient.sendMessage(CloudConstant.MQ_JEECG_PLACE_ORDER, map);
        rabbitMqClient.sendMessage(CloudConstant.MQ_JEECG_PLACE_ORDER_TIME, map, 10);
        return Result.OK("MQ发送消息成功");
    }

    @GetMapping(value = "/rabbitmq2")
    @Operation(summary = "rabbitmq消息总线测试")
    public Result<?> rabbitmq2(HttpServletRequest req) {
        //rabbitmq消息总线测试
        BaseMap params = new BaseMap();
        params.put("orderId", "123456");
        // >>> ApplicationEventPublisher#publishEvent 只在当前 Spring 容器内广播事件，默认不会跨服务、跨进程。
        rabbitMqClient.publishEvent(CloudConstant.MQ_DEMO_BUS_EVENT, params);
        return Result.OK("MQ发送消息成功");
    }


    @Autowired
    private RabbitTemplate rabbitTemplate;
    public static final String EXCHANGE_DIRECT = "exchange.direct.order";
    public static final String ROUTING_KEY = "order";

    @GetMapping(value = "/testSendMessage")
    @Operation(summary = "测试发送消息")
    public Result<?> rabbitmqToExchange(HttpServletRequest req) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(
                EXCHANGE_DIRECT,
                ROUTING_KEY,
                "Hello zhuangpf",
                correlationData);
        return Result.OK("MQ发送消息成功");
    }

    // --------------------------------------------------------------------------- //
    public final static String EXCHANGE_NORMAL = "exchange.normal.video";
    public final static String ROUTING_KEY_NORMAL = "routing.key.normal.video";

    @GetMapping(value = "/testSendMessageButReject")
    @Operation(summary = "测试消息发送拒收")
    public void testSendMessageButReject() {
        rabbitTemplate
                .convertAndSend(
                        EXCHANGE_NORMAL,
                        ROUTING_KEY_NORMAL,
                        "测试死信情况 1：消息被拒绝");
    }

    @GetMapping(value = "/testSendMultiMessage")
    @Operation(summary = "测试消息发送大于队列最大容量")
    public void testSendMultiMessage() {
        for (int i = 0; i < 20; i++) {
            rabbitTemplate.convertAndSend(
                    EXCHANGE_NORMAL,
                    ROUTING_KEY_NORMAL,
                    "测试死信情况 2：消息数量超过队列的最大容量" + i);
        }
    }

}
