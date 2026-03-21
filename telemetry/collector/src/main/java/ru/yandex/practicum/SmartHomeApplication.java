package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class SmartHomeApplication {

    public static void main(String[] args) {
        //SpringApplication.run(SmartHomeApplication.class, args);

        ConfigurableApplicationContext context = SpringApplication.run(SmartHomeApplication.class, args);

        // Получаем бин AggregationStarter из контекста и запускаем основную логику сервиса
        EventDataProducer eventDataProducer = context.getBean(EventDataProducer.class);
        eventDataProducer.sendEvents();
    }

}
