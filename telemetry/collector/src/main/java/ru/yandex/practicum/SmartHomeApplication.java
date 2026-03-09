package ru.yandex.practicum;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
//import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;

@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class SmartHomeApplication {

    public static void main(String[] args) {
        //SpringApplication.run(SmartHomeApplication.class, args);

        ConfigurableApplicationContext context = SpringApplication.run(SmartHomeApplication.class, args);

    }

}
