package ru.springboot.leondemo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.CannotCreateTransactionException;
import ru.springboot.leondemo.dto.TimeEvent;
import ru.springboot.leondemo.dto.TimeScheduleDto;
import ru.springboot.leondemo.entity.TimeSchedule;
import ru.springboot.leondemo.repository.TimeScheduleRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeScheduleServiceUnitTest {

    @Mock
    private TimeScheduleRepository timeScheduleRepository;

    @Mock
    private Acknowledgment acknowledgment;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TimeScheduleService timeScheduleService;

    @Value("${kafka.topic:time-ticks}")
    private String topic;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(timeScheduleService, "topic", "time-ticks");
    }

    @Test
    @DisplayName("While calling consume - then save TimeSchedule")
    public void givenTimeEvent_whenCallConsume_thenTimeRepositorySaveTimeSchedule() {
        //given
        TimeEvent timeEvent = new TimeEvent();
        timeEvent.setTime(LocalDateTime.now());

        //when
        timeScheduleService.consume(timeEvent, acknowledgment);

        //then
        verify(timeScheduleRepository, times(1))
                .saveTimeSchedule(any(UUID.class), eq(timeEvent.getTime()));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("While calling consume, throw CannotCreateTransactionException - then ack.nack")
    public void givenTimeEvent_whenCallConsumeAndThrowCCTE_thenAckNack() {
        //given
        TimeEvent timeEvent = new TimeEvent();
        timeEvent.setTime(LocalDateTime.now());

        //when
        doThrow(CannotCreateTransactionException.class)
                .when(timeScheduleRepository).saveTimeSchedule(any(UUID.class), eq(timeEvent.getTime()));
        timeScheduleService.consume(timeEvent, acknowledgment);

        //then
        verify(timeScheduleRepository, times(1))
                .saveTimeSchedule(any(UUID.class), eq(timeEvent.getTime()));

        verify(acknowledgment, times(1)).nack(Duration.ofSeconds(6));
    }

    @Test
    @DisplayName("While calling consume, throw DuplicateKeyException - then ack.nack")
    public void givenTimeEvent_whenCallConsumeAndThrowDAE_thenAckNack() {
        //given
        TimeEvent timeEvent = new TimeEvent();
        timeEvent.setTime(LocalDateTime.now());

        //when
        doThrow(DuplicateKeyException.class)
                .when(timeScheduleRepository).saveTimeSchedule(any(UUID.class), eq(timeEvent.getTime()));
        timeScheduleService.consume(timeEvent, acknowledgment);

        //then
        verify(timeScheduleRepository, times(1))
                .saveTimeSchedule(any(UUID.class), eq(timeEvent.getTime()));

        verify(acknowledgment, times(1)).nack(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("While calling consume, throw Exception - then ack.nack")
    public void givenTimeEvent_whenCallConsumeAndThrowE_thenAckNack() {
        //given
        TimeEvent timeEvent = new TimeEvent();
        timeEvent.setTime(LocalDateTime.now());

        //when
        doThrow(RuntimeException.class)
                .when(timeScheduleRepository).saveTimeSchedule(any(UUID.class), eq(timeEvent.getTime()));
        timeScheduleService.consume(timeEvent, acknowledgment);

        //then
        verify(timeScheduleRepository, times(1))
                .saveTimeSchedule(any(UUID.class), eq(timeEvent.getTime()));

        verify(acknowledgment, times(1)).nack(Duration.ofSeconds(2));
    }

    @Test
    @DisplayName("While calling getAllTimeSchedules method should return List time dto")
    public void givenPageable_whenCallingGetAllTimeSchedules_thenReturnListTimeScheduleDto() {
        //given
        int page = 0;
        int size = 10;

        Page<TimeSchedule> timeSchedulePage = Page.empty(PageRequest.of(page, size));

        //when
        when(timeScheduleRepository.findAll(any(Pageable.class))).thenReturn(timeSchedulePage);
        List<TimeScheduleDto> result = timeScheduleService.getAllTimeSchedules(page, size);

        //then
        verify(timeScheduleRepository, times(1)).findAll(any(Pageable.class));

        assertThat(result).isEmpty();
        assertThat(result).hasSize(0);
        assertThat(result).isInstanceOf(List.class);
    }
}
