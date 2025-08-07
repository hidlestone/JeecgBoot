package org.jeecg.modules.test.rabbitmq.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author zhuangpf
 * @date 2025-03-25
 */
@Component
@Slf4j
public class MyMessageListener {

    public static final String EXCHANGE_DIRECT = "exchange.direct.order";
    public static final String ROUTING_KEY = "order";
    public static final String QUEUE_NAME = "queue.order";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QUEUE_NAME, durable = "true"),
            exchange = @Exchange(value = EXCHANGE_DIRECT),
            key = {ROUTING_KEY}
    ))
    public void processMessage(String dateString,
                               Message message,
                               Channel channel) throws IOException {
        log.info(dateString);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 3、给 RabbitMQ 服务器返回 ACK 确认信息
        channel.basicAck(deliveryTag, false);
    }
}
