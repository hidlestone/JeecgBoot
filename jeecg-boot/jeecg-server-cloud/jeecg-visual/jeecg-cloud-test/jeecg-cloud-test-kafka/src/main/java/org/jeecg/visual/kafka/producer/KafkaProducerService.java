package org.jeecg.visual.kafka.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate,
                                @Value("${demo.kafka.topic}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void send(String key, String message) {
        CompletableFuture<?> future = kafkaTemplate.send(topicName, key, message).completable();
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send message to Kafka. topic={}, key={}, message={}", topicName, key, message, ex);
            } else {
                log.info("Sent message to Kafka. topic={}, key={}, message={}", topicName, key, message);
            }
        });
    }
} 