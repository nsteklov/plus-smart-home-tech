package ru.yandex.practicum.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaPropertiesConfig {
    private String bootstrapServers;
    private String keySerializer;
    private String valueSerializer;
    private String sensorEventTopic;
    private String hubEventTopic;

    public String getKeySerializer() {
        return keySerializer;
    }

    public void setKeySerializer(String keySerializer) {
        this.keySerializer = keySerializer;
    }

    public String getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(String valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getSensorEventTopic() {
        return sensorEventTopic;
    }

    public void setSensorEventTopic(String sensorEventTopic) {
        this.sensorEventTopic = sensorEventTopic;
    }
    public String getHubEventTopic() {
        return hubEventTopic;
    }

    public void setHubEventTopic(String hubEventTopic) {
        this.hubEventTopic = hubEventTopic;
    }
}
