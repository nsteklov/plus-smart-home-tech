package ru.yandex.practicum.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka")
@Getter
@Setter
public class KafkaPropertiesConfig {
    private String bootstrapServers;
    private String keySerializer;
    private String valueSerializer;
    private String sensorEventTopic;
    private String hubEventTopic;
}
