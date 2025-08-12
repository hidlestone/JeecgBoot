package org.jeecg.visual.kafka.controller;

import org.jeecg.visual.kafka.producer.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaDemoController {

    private final KafkaProducerService producerService;

    public KafkaDemoController(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    @GetMapping("/send")
    public ResponseEntity<String> send(@RequestParam("message") String message,
                                       @RequestParam(value = "key", required = false) String key) {
        if (!StringUtils.hasText(message)) {
            return ResponseEntity.badRequest().body("message must not be empty");
        }
        producerService.send(key, message);
        return ResponseEntity.ok("sent");
    }
} 