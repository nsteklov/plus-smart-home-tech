package ru.yandex.practicum.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aggregator")
@Getter
@Setter
public class KafkaPropertiesConfigAggregator {
    private String bootstrapServers;
    private String clientId;
    private String groupId;
    private String keyDeserializer;
    private String valueDeserializer;
    private String sensorEventTopic;
    private String snapshotTopic;
    private int maxPollRecordsConfig;
    private int fetchMaxBytesConfig;
    private int maxPartitionFetchBytesConfig;
    private String keySerializer;
    private String valueSerializer;
}