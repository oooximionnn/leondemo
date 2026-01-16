package ru.springboot.leondemo.integration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.springboot.leondemo.integration.IntegrationTestBase;
import ru.springboot.leondemo.dto.TimeScheduleDto;
import ru.springboot.leondemo.repository.TimeScheduleRepository;
import ru.springboot.leondemo.service.TimeScheduleService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public class TimeScheduleServiceIT extends IntegrationTestBase {

    private final TimeScheduleService timeScheduleService;

    private final TimeScheduleRepository timeScheduleRepository;

    @BeforeEach
    void cleanDb() {
        timeScheduleRepository.deleteAll();
    }

    @Test
    @DisplayName("Test DB has rows, after app starting")
    void getTimeSchedulesWithoutError() {
        int page = 0;
        int size = 100;

        await()
                .atMost(10, SECONDS)
                .pollInterval(3, SECONDS)
                .untilAsserted(() -> {
                    List<TimeScheduleDto> resultAfter = timeScheduleService.getAllTimeSchedules(page, size);
                    assertThat(resultAfter).isNotEmpty();
                    assertThat(resultAfter).hasSizeGreaterThanOrEqualTo(1);
                    log.error(resultAfter.toString());
                });
    }

    @Test
    @DisplayName("""
            Тест с отключением кафка.
            1) Даем поработать шедулеру. Проверяем, что в таблице есть данные
            2) Останавливаем контейнер кафка на 5 секунд. Запускаем контейнер кафка
            3) Проверяем, что размер таблицы после запуска контейнера больше чем во время его простоя
            4) Проверяем, что в таблице значения времени в отсортированном порядке и с ~ секундной задержкой
           
            """)
    void testDatabaseHasRowsAfterKafkaOutage() throws InterruptedException {
        int page = 0;
        int size = 100;

        await()
                .atMost(10, SECONDS)
                .untilAsserted(() -> {
                    List<TimeScheduleDto> result = timeScheduleService.getAllTimeSchedules(page, size);
                    assertThat(result).hasSizeGreaterThanOrEqualTo(1);
                });

        IntegrationTestBase.pause(IntegrationTestBase.kafkaContainer);

        Thread.sleep(5000);

        List<TimeScheduleDto> resultWhileContainerStoping =
                timeScheduleService.getAllTimeSchedules(page, size);

        assertThat(resultWhileContainerStoping).hasSizeGreaterThanOrEqualTo(1);

        IntegrationTestBase.unpause(IntegrationTestBase.kafkaContainer);

        await().atMost(40, SECONDS)
                .untilAsserted(() -> {
                    List<TimeScheduleDto> result =
                            timeScheduleService.getAllTimeSchedules(0, 100);
                    assertThat(result).hasSizeGreaterThanOrEqualTo(13);

                    assertThat(result.size()).isGreaterThan(resultWhileContainerStoping.size());

                    List<LocalDateTime> times = result.stream()
                            .map(TimeScheduleDto::getTime)
                            .toList();

                    assertThat(times).isSorted();

                    Duration delta1 = Duration.between(times.get(0), times.get(1));
                    Duration delta2 = Duration.between(times.get(1), times.get(2));
                    Duration delta3 = Duration.between(times.get(2), times.get(3));
                    Duration delta4 = Duration.between(times.get(3), times.get(4));
                    Duration delta5 = Duration.between(times.get(4), times.get(5));
                    Duration delta6 = Duration.between(times.get(5), times.get(6));
                    Duration delta7 = Duration.between(times.get(6), times.get(7));
                    Duration delta8 = Duration.between(times.get(7), times.get(8));
                    Duration delta9 = Duration.between(times.get(8), times.get(9));
                    Duration delta10 = Duration.between(times.get(9), times.get(10));
                    Duration delta11 = Duration.between(times.get(10), times.get(11));
                    Duration delta12 = Duration.between(times.get(11), times.get(12));

                    assertThat(delta1).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta2).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta3).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta4).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta5).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta6).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta7).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta8).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta9).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta10).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta11).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta12).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                });
    }

    @Test
    @DisplayName("""
            Тест с отключением postgres.
            1) Даем поработать шедулеру. Проверяем, что в таблице есть данные
            2) Останавливаем контейнер postgres на 5 секунд. Запускаем контейнер postgres
            3) Проверяем, что в таблице значения времени в отсортированном порядке и с ~ секундной задержкой
           
            """)
    void testDatabaseHasRowsAfterPostgresOutage() throws InterruptedException {
        int page = 0;
        int size = 100;

        await()
                .atMost(10, SECONDS)
                .untilAsserted(() -> {
                    List<TimeScheduleDto> result = timeScheduleService.getAllTimeSchedules(page, size);
                    assertThat(result).hasSizeBetween(1, 12);
                });

        IntegrationTestBase.pause(IntegrationTestBase.postgreSQLContainer);

        Thread.sleep(5000);

        IntegrationTestBase.unpause(IntegrationTestBase.postgreSQLContainer);

        await().pollDelay(Duration.ofSeconds(5))
                .atMost(40, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    List<TimeScheduleDto> result =
                            timeScheduleService.getAllTimeSchedules(0, 100);
                    assertThat(result).hasSizeGreaterThanOrEqualTo(13);

                    List<LocalDateTime> times = result.stream()
                            .map(TimeScheduleDto::getTime)
                            .toList();

                    assertThat(times).isSorted();

                    Duration delta1 = Duration.between(times.get(0), times.get(1));
                    Duration delta2 = Duration.between(times.get(1), times.get(2));
                    Duration delta3 = Duration.between(times.get(2), times.get(3));
                    Duration delta4 = Duration.between(times.get(3), times.get(4));
                    Duration delta5 = Duration.between(times.get(4), times.get(5));
                    Duration delta6 = Duration.between(times.get(5), times.get(6));
                    Duration delta7 = Duration.between(times.get(6), times.get(7));
                    Duration delta8 = Duration.between(times.get(7), times.get(8));
                    Duration delta9 = Duration.between(times.get(8), times.get(9));
                    Duration delta10 = Duration.between(times.get(9), times.get(10));
                    Duration delta11 = Duration.between(times.get(10), times.get(11));
                    Duration delta12 = Duration.between(times.get(11), times.get(12));

                    assertThat(delta1).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta2).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta3).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta4).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta5).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta6).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta7).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta8).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta9).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta10).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta11).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                    assertThat(delta12).isBetween(Duration.ofMillis(900), Duration.ofMillis(1100));
                });
    }
}




