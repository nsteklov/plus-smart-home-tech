package ru.yandex.practicum.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analyzer")
@Getter
@Setter
public class KafkaPropertiesConfigAnalyzer {
    private String bootstrapServers;
    private String clientId;
    private String groupId;
    private String keyDeserializer;
    private String hubValueDeserializer;
    private String snapshotValueDeserializer;
    private String hubEventTopic;
    private String snapshotTopic;
    private int maxPollRecordsConfig;
    private int fetchMaxBytesConfig;
    private int maxPartitionFetchBytesConfig;
    private String keySerializer;
    private String valueSerializer;
}