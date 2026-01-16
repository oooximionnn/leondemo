package ru.springboot.leondemo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Конфигурация шедулера
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(10);
        ts.setThreadNamePrefix("sched-");
        ts.initialize();
        return ts;
    }
}