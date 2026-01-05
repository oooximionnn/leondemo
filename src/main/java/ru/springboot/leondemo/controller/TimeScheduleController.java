package ru.springboot.leondemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.springboot.leondemo.service.TimeScheduleService;

@RestController
@RequestMapping("/api/v1/timeschedules")
@RequiredArgsConstructor
@Tag(name = "Контроллер для работы с TimeSchedule")
public class TimeScheduleController {

    private final TimeScheduleService timeScheduleService;

    @Operation(summary = "Получить список TimeScheduleDto",
            description = "Для получения необходимо передать page и size")
    @GetMapping
    public ResponseEntity<?> getAllTimeSchedules(
            @RequestParam(required = true) int page,
            @RequestParam(required = true) int size) {
        return ResponseEntity.ok(timeScheduleService.getAllTimeSchedules(page, size));
    }
}
