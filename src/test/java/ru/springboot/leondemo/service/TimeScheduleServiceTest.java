package ru.springboot.leondemo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;
import ru.springboot.leondemo.dto.TimeEvent;
import ru.springboot.leondemo.repository.TimeRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeScheduleServiceUnitTest {

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private TimeScheduleService timeScheduleService;

    @Value("${kafka.topic:time-ticks}")
    private String topic;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(timeScheduleService, "topic", "time-ticks");
    }

    @Test
    @DisplayName("While calling tick - then call kafka")
    public void whenCallTick_thenKafkaProducerCallSend() {
        //when
        timeScheduleService.tick();

        //then
        verify(kafkaProducer, times(1))
                .send(eq("time-ticks"), eq("first_partition"), any(TimeEvent.class));
    }

}
