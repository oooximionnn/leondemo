package ru.springboot.leondemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import ru.springboot.leondemo.dto.TimeEvent;
import ru.springboot.leondemo.dto.TimeScheduleDto;
import ru.springboot.leondemo.entity.TimeSchedule;
import ru.springboot.leondemo.repository.TimeRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Сервис для работы с TimeSchedule
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeScheduleService {
    private final TimeRepository timeRepository;
    private final KafkaProducer kafkaProducer;

    @Value("${kafka.topic}")
    private String topic;

    @Scheduled(fixedRate = 1000,
            initialDelay = 5000)
    public void tick() {
        TimeEvent event = new TimeEvent();
        event.setTime(LocalDateTime.now());

        kafkaProducer.send(topic, "first_partition", event);
    }

    @KafkaListener(
            topics = "${kafka.topic}",
            groupId = "${kafka.consumer.group-id}"
    )
    public void consume(TimeEvent timeEvent, Acknowledgment ack) {

        TimeSchedule entity = new TimeSchedule();
        entity.setTime(timeEvent.getTime());
        try {
            timeRepository.save(entity);
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

        return timeRepository.findAll(pageable)
                .map(TimeSchedule::fromEntity)
                .getContent();
    }
}
