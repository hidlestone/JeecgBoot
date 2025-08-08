package org.jeecg.modules.test.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DeadLetterConfig {

    // 1. 原队列 - 配置死信交换机
    @Bean
    public Queue originalQueue() {
        return QueueBuilder.durable("original-queue")
                .deadLetterExchange("dlx-exchange")
                .deadLetterRoutingKey("dlx-routing-key")
                .maxLength(10)
                .ttl(10000)
                .build();
    }

    // 2. 原交换机
    @Bean
    public Exchange originalExchange() {
        return new DirectExchange("original-exchange", true, false);
    }

    // 3. 原队列绑定
    @Bean
    public Binding originalBinding() {
        return BindingBuilder.bind(originalQueue())
                .to(originalExchange())
                .with("original-routing-key").noargs();
    }

    // 4. 死信队列 - 不需要死信配置
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("dlq-queue").build();
    }

    // 5. 死信交换机
    @Bean
    public Exchange deadLetterExchange() {
        return new DirectExchange("dlx-exchange", true, false);
    }

    // 6. 死信队列绑定
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlx-routing-key").noargs();
    }

}