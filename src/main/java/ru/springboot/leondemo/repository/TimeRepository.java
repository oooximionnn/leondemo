package ru.springboot.leondemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.springboot.leondemo.entity.TimeSchedule;

import java.util.UUID;

public interface TimeRepository extends JpaRepository<TimeSchedule, UUID> {
}
