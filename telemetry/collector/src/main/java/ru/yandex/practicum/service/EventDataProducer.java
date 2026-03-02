//package ru.yandex.practicum.service;
//
//import com.google.protobuf.Timestamp;
//import net.devh.boot.grpc.client.inject.GrpcClient;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
//import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerOuterClass;
//import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
//import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
//import ru.yandex.practicum.model.TemperatureSensorEvent;
//
//import java.time.Instant;
//
//@Component
//public class EventDataProducer {
//
//    private static final Logger log = LoggerFactory.getLogger(EventDataProducer.class);
//
//    @GrpcClient("collector")
//    private CollectorControllerGrpc.CollectorControllerBlockingStub collectorStub;
//
//    private SensorEventProto createTemperatureSensorEvent(TemperatureSensorEvent sensor) {
//        int temperatureCelsius = getRandomSensorValue(sensor.getTemperatureC());
//        int temperatureFahrenheit = (int) (temperatureCelsius * 1.8 + 32);
//        Instant ts = Instant.now();
//
//
//        return SensorEventProto.newBuilder()
//                .setId(sensor.getId())
//                .setTimestamp(Timestamp.newBuilder()
//                        .setSeconds(ts.getEpochSecond())
//                        .setNanos(ts.getNano())
//                ).setTemperatureSensor(
//                        TemperatureSensorProto.newBuilder()
//                                .setTemperatureC(temperatureCelsius)
//                                .setTemperatureF(temperatureFahrenheit)
//                                .build()
//                )
//                .build();
//    }
//
//    private int getRandomSensorValue(int prevValue) {
//        //Например вернем число в диапазоне от prevValue - 20 до prevValue + 10
//        return (int) (Math.random() * (20)) + prevValue - 10;
//    }
//
//    private void sendEvent(SensorEventProto event) {
//        log.info("Отправляю данные: {}", event.getAllFields());
//        CollectorControllerOuterClass.CollectorResponse response = collectorStub.collectSensorEvent(event);
//        log.info("Получил ответ от коллектора: {}", response);
//    }
//}
