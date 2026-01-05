package ru.springboot.leondemo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.springboot.leondemo.dto.KafkaEvent;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, String key, KafkaEvent event) {
        kafkaTemplate.send(topic, key, event);
    }
}