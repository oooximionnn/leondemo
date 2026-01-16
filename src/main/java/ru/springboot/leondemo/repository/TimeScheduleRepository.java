package ru.springboot.leondemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.springboot.leondemo.entity.TimeSchedule;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeScheduleRepository extends JpaRepository<TimeSchedule, UUID> {

    @Query(
            value =
                    "INSERT INTO time (id, time) " +
                    "VALUES (:id, :time) " +
                    "ON CONFLICT (time) DO NOTHING " +
                    "RETURNING id ",
            nativeQuery = true
    )
    Optional<UUID> saveTimeSchedule(@Param("id") UUID id,
                                    @Param("time") LocalDateTime time);
}
