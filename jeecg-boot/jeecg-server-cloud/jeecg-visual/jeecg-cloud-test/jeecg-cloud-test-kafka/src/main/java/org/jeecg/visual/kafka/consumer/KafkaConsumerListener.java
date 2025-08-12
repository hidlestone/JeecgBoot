package org.jeecg.visual.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerListener.class);

    @KafkaListener(topics = "${demo.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(@Payload String value,
                          @Header(name = "kafka_receivedTopic", required = false) String topic,
                          @Header(name = "kafka_receivedMessageKey", required = false) String key,
                          @Header(name = "kafka_offset", required = false) Long offset,
                          @Header(name = "kafka_receivedPartitionId", required = false) Integer partition) {
        log.info("Consumed message from Kafka. topic={}, partition={}, offset={}, key={}, value={}",
                topic, partition, offset, key, value);
    }
} 