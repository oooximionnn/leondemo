package ru.springboot.leondemo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Класс события фиксированного текущего времени
 */
@Data
public class TimeEvent extends KafkaEvent {
    private LocalDateTime time;
}
