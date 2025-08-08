package org.jeecg.modules.test.rabbitmq.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DeadLetterListener {

    public final static String EXCHANGE_NORMAL = "exchange.normal.video";
    public final static String QUEUE_NORMAL = "queue.normal.video";
    public final static String ROUTING_KEY_NORMAL = "routing.key.normal.video";

    public final static String QUEUE_DEAD_LETTER = "queue.dead.letter.video";

    /**
     * 监听正常队列
     */
    /*@RabbitListener(queues = {QUEUE_NORMAL})
    public void processMessageNormal(Message message, Channel channel) throws IOException {
        // 监听正常队列，但是拒绝消息
        log.info("★[normal] 消息接收到，但我拒绝。");
        channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
    }*/
    @RabbitListener(queues = {QUEUE_NORMAL})
    public void processMessageNormal(Message message, Channel channel) throws IOException {
        // 监听正常队列
        log.info("★[normal] 消息接收到。");
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 监听私信队列
     */
    @RabbitListener(queues = {QUEUE_DEAD_LETTER})
    public void processMessageDead(String dataString, Message message, Channel channel) throws IOException {
        log.info("★------------------------------------------------★");
        log.info("★[dead letter] dataString =" + dataString);
        log.info("★[dead letter] 我是死信监听方法，我接收到了死信消息");

        // 获取消息属性
        MessageProperties properties = message.getMessageProperties();
        // 获取原队列名称
        String originalQueue = properties.getHeaders().get("x-first-death-queue").toString();
        log.info("原队列名称: " + originalQueue);
        // 获取死信原因
        String deathReason = properties.getHeaders().get("x-death").toString();
        log.info("死信原因: " + deathReason);
        // 获取路由键
        String routingKey = properties.getReceivedRoutingKey();
        log.info("路由键: " + routingKey);
        // 获取交换机名称
        String exchange = properties.getReceivedExchange();
        log.info("交换机名称: " + exchange);
        // 获取消息ID
        String messageId = properties.getMessageId();
        log.info("消息ID: " + messageId);
        // 获取时间戳
        Date timestamp = properties.getTimestamp();
        log.info("消息时间戳: " + timestamp);
        // 获取用户ID
        String userId = properties.getUserId();
        log.info("用户ID: " + userId);
        // 获取应用ID
        String appId = properties.getAppId();
        log.info("应用ID: " + appId);
        // 获取消息类型
        String type = properties.getType();
        log.info("消息类型: " + type);
        // 获取优先级
        Integer priority = properties.getPriority();
        log.info("消息优先级: " + priority);
        // 获取延迟时间
        Integer delay = properties.getDelay();
        log.info("延迟时间: " + delay);

        // 获取死信详细信息
        List<Map<String, Object>> deathRecords = (List<Map<String, Object>>) properties.getHeaders().get("x-death");
        if (deathRecords != null && !deathRecords.isEmpty()) {
            Map<String, Object> firstDeath = deathRecords.get(0);
            log.info("第一次死亡队列: " + firstDeath.get("queue"));
            log.info("死亡原因: " + firstDeath.get("reason"));
            log.info("死亡时间: " + firstDeath.get("time"));
            log.info("路由键: " + firstDeath.get("routing-keys"));
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 原队列监听器
     * 处理正常业务消息，失败时消息会变成死信
     */
    @RabbitListener(queues = {"original-queue"})
    public void handleOriginalMessage(String dataString, Message message, Channel channel) throws IOException {
        // 死信队列会自动创建
        log.info("★[normal] 消息接收到，但我拒绝。");
        channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 死信队列监听器
     * 处理从原队列发送过来的死信消息
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "dlq-queue", durable = "true"), // 死信队列
                    exchange = @Exchange(value = "dlx-exchange", type = ExchangeTypes.DIRECT, durable = "true"),
                    key = "dlx-routing-key"
            )
    )
    public void handleDeadLetterMessage(String dataString, Message message, Channel channel) throws IOException {
        log.info("★------------------------------------------------★");
        log.info("★[dead letter] dataString =" + dataString);
        log.info("★[dead letter] 我是死信监听方法，我接收到了死信消息");
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
