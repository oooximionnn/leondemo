package ru.springboot.leondemo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TimeScheduleDto {
    private LocalDateTime time;
}
