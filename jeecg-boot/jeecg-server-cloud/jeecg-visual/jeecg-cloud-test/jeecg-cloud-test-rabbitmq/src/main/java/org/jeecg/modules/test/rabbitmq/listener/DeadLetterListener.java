package org.jeecg.modules.test.rabbitmq.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
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
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
