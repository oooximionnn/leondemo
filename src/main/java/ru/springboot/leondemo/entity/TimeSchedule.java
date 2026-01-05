package ru.springboot.leondemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.springboot.leondemo.dto.TimeScheduleDto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность текущего времени
 */
@Data
@Entity
@Table(name = "time")
@NoArgsConstructor
@AllArgsConstructor
public class TimeSchedule {
    @Id
    @Column(name = "id",  nullable = false, updatable = false)
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "time",
            nullable = false,
            unique = true)
    private LocalDateTime time;

    //TODO mupstruct применить
    public static TimeScheduleDto fromEntity(TimeSchedule timeSchedule) {
        TimeScheduleDto timeScheduleDto = new TimeScheduleDto();
        timeScheduleDto.setTime(timeSchedule.getTime());
        return timeScheduleDto;
    }
}
