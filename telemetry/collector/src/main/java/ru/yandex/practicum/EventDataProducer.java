package ru.yandex.practicum;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;

@Component
@Slf4j
public class EventDataProducer {

    @GrpcClient("collector")
    private CollectorControllerGrpc.CollectorControllerBlockingStub collectorStub;

    public void sendEvents() {

        //Температурный датчик 1
        SensorEventProto temperatureSensorProto = SensorEventProto.newBuilder()
                .setId("123")
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(1234)
                        .setNanos(5234234)
                ).setTemperatureSensor(
                        TemperatureSensorProto.newBuilder()
                                .setTemperatureC(12345)
                                .setTemperatureF(234234)
                                .build()
                )
                .setHubId("234")
                .build();
        log.info("Отправляю данные 1: {}", temperatureSensorProto.getAllFields());
        collectorStub.collectSensorEvent(temperatureSensorProto);

        //Температурный датчик 2 такой же
        SensorEventProto temperatureSensorProto2 = SensorEventProto.newBuilder()
                .setId("123")
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(1234)
                        .setNanos(5234234)
                ).setTemperatureSensor(
                        TemperatureSensorProto.newBuilder()
                                .setTemperatureC(12345)
                                .setTemperatureF(234234)
                                .build()
                )
                .setHubId("234")
                .build();
        log.info("Отправляю данные 2: {}", temperatureSensorProto2.getAllFields());
        collectorStub.collectSensorEvent(temperatureSensorProto2);

        //Температурный датчик 3
        SensorEventProto temperatureSensorProto3 = SensorEventProto.newBuilder()
                .setId("123")
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(1234)
                        .setNanos(5234234)
                ).setTemperatureSensor(
                        TemperatureSensorProto.newBuilder()
                                .setTemperatureC(123456)
                                .setTemperatureF(234234)
                                .build()
                )
                .setHubId("234")
                .build();
        log.info("Отправляю данные 3: {}", temperatureSensorProto3.getAllFields());
        collectorStub.collectSensorEvent(temperatureSensorProto3);

        //Переключатель
        SensorEventProto switchSensorProto1 = SensorEventProto.newBuilder()
                .setId("123")
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(1234)
                        .setNanos(5234234)
                ).setSwitchSensor(
                        SwitchSensorProto.newBuilder()
                                .setState(true)
                                .build()
                )
                .setHubId("234")
                .build();
        log.info("Отправляю данные 3: {}", temperatureSensorProto3.getAllFields());
        collectorStub.collectSensorEvent(temperatureSensorProto3);
    }

}
