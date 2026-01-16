package ru.springboot.leondemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import ru.springboot.leondemo.dto.TimeEvent;
import ru.springboot.leondemo.dto.TimeScheduleDto;
import ru.springboot.leondemo.entity.TimeSchedule;
import ru.springboot.leondemo.repository.TimeScheduleRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Сервис для работы с TimeSchedule
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeScheduleService {
    private final TimeScheduleRepository timeScheduleRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Queue<TimeEvent> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean draining = new AtomicBoolean(false);

    @Value("${kafka.topic}")
    private String topic;

    @Scheduled(cron = "*/1 * * * * *")
    public void tick() {
        TimeEvent event = new TimeEvent();
        event.setTime(LocalDateTime.now());

        queue.offer(event);
    }

    @Scheduled(fixedDelay = 200, initialDelay = 3000)
    public void flush() {
        if (!draining.compareAndSet(false, true)) return;
        drainNext();
    }

    private void drainNext() {
        TimeEvent event = queue.peek();
        if (event == null) {
            draining.set(false);
            return;
        }

        try {
            kafkaTemplate.send(topic, "first_partition", event)
                    .whenComplete((res, ex) -> {
                        if (ex == null) {
                            queue.poll();
                            drainNext();
                        } else {
                            draining.set(false);
                            log.warn("[SEND FAIL async] will retry same head. event={}", event, ex);
                        }
                    });
        } catch (Exception ex) {
            draining.set(false);
            log.warn("[SEND FAIL sync] will retry same head. event={}", event, ex);
        }
    }

    @KafkaListener(
            topics = "${kafka.topic}",
            groupId = "${kafka.consumer.group-id}"
    )
    public void consume(TimeEvent timeEvent, Acknowledgment ack) {

        TimeSchedule entity = new TimeSchedule();
        entity.setId(UUID.randomUUID());
        entity.setTime(timeEvent.getTime());
        try {
            log.info("Consumer получил: {}", timeEvent);
            timeScheduleRepository.saveTimeSchedule(entity.getId(), entity.getTime());
            ack.acknowledge();
            log.info("Время сохранено {}", entity.getTime());

        } catch (CannotCreateTransactionException e) {
            log.error("БД недоступна: {}", e.getMessage());
            ack.nack(Duration.ofSeconds(6));
        } catch (DataAccessException e) {
            log.error("БД ошибка (JPA/Hibernate): {}", e.getMessage());
            ack.nack(Duration.ofSeconds(5));
        } catch (Exception e) {
            log.error("Неожиданная ошибка: {}", e.getMessage(), e);
            ack.nack(Duration.ofSeconds(2));
        }
    }

    /**
     * Получение TimeSchedule с пагинацией.
     */
    public List<TimeScheduleDto> getAllTimeSchedules(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size
        );

        return timeScheduleRepository.findAll(pageable)
                .map(TimeSchedule::fromEntity)
                .getContent();
    }
}
