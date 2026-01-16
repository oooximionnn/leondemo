package ru.springboot.leondemo.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import ru.springboot.leondemo.integration.annotation.IT;

@IT
public abstract class IntegrationTestBase {

    protected static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:18")
                    .withDatabaseName("postgres")
                    .withPassword("postgres")
                    .withUsername("postgres");

    protected static final KafkaContainer kafkaContainer =
            new KafkaContainer("apache/kafka-native:3.8.0");

    @BeforeAll
    static void runContainers() {
        postgreSQLContainer.start();
        kafkaContainer.start();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    protected static void pause(Container container) {
        DockerClientFactory.instance().client().pauseContainerCmd(container.getContainerId()).exec();
    }

    protected static void unpause(Container container) {
        DockerClientFactory.instance().client().unpauseContainerCmd(container.getContainerId()).exec();
    }
}
