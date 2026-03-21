package ru.yandex.practicum.configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("collector")
@Getter
@Setter
public class KafkaPropertiesConfig {
    private String bootstrapServers;
    private Producer producer = new Producer();
    private Topics topics = new Topics();

    @Data
    public static class Producer {
        private String keySerializer;
        private String valueSerializer;
    }

    @Data
    public static class Topics {
        private String sensorEventTopic;
        private String hubEventTopic;
    }
}
