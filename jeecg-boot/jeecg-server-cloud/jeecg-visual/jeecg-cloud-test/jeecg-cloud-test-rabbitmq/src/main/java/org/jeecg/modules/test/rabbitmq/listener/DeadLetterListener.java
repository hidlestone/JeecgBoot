package org.jeecg.modules.test.rabbitmq.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
    @RabbitListener(queues = {QUEUE_NORMAL})
    public void processMessageNormal(Message message, Channel channel) throws IOException {
        // 监听正常队列，但是拒绝消息
        log.info("★[normal] 消息接收到，但我拒绝。");
        channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 监听私信队列
     */
    @RabbitListener(queues = {QUEUE_DEAD_LETTER})
    public void processMessageDead(String dataString, Message message, Channel channel) throws IOException {
        // 监听死信队列
        log.info("★[dead letter] dataString =" + dataString);
        log.info("★[dead letter] 我是死信监听方法，我接收到了死信消息");

        // 获取消息属性
        MessageProperties properties = message.getMessageProperties();

        // 获取死信原因
        String deadLetterReason = properties.getHeader("x-dead-letter-reason");

        // 获取原始队列名称
        String originalQueue = properties.getHeader("x-first-death-queue");

        // 获取原始交换机
        String originalExchange = properties.getHeader("x-first-death-exchange");

        // 获取原始路由键
        String originalRoutingKey = properties.getHeader("x-first-death-reason");

        log.info("★[dead letter] 消息内容: {}", dataString);
        log.info("★[dead letter] 死信原因: {}", deadLetterReason);
        log.info("★[dead letter] 原始队列: {}", originalQueue);
        log.info("★[dead letter] 原始交换机: {}", originalExchange);
        log.info("★[dead letter] 原始路由键: {}", originalRoutingKey);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
