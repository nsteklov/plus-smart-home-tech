package ru.yandex.practicum;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.ArrayList;
import java.util.List;

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

        //SCENARIO_ADDED
        List<ScenarioConditionProto> scenarioConditionsProto = new ArrayList<>();
        ScenarioConditionProto scenarioConditionProto = ScenarioConditionProto.newBuilder()
                .setSensorId("432")
                .setType(ConditionTypeProto.CO2LEVEL)
                .setOperation(ConditionOperationProto.EQUALS)
                .setIntValue(432)
                .build();
        scenarioConditionsProto.add(scenarioConditionProto);

        List<DeviceActionProto> deviceActionsProto = new ArrayList<>();
        DeviceActionProto deviceActionProto = DeviceActionProto.newBuilder()
                .setSensorId("432")
                .setType(ActionTypeProto.SET_VALUE)
                .setValue(4322)
                .build();
        deviceActionsProto.add(deviceActionProto);

        HubEventProto scenarioAddedEventProto = HubEventProto.newBuilder()
                .setHubId("123")
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(1234)
                        .setNanos(5234234)
                ).setScenarioAdded(
                        ScenarioAddedEventProto.newBuilder()
                                .setName("Test")
                                .addCondition(scenarioConditionProto)
                                .addAction(deviceActionProto)
                )
                .build();
        log.info("Отправляю данные 4: {}", scenarioAddedEventProto.getAllFields());
        collectorStub.collectHubEvent(scenarioAddedEventProto);

//        //DEVICE_ADDED
//        HubEventProto deviceAddedEventProto = HubEventProto.newBuilder()
//                .setHubId("33")
//                .setTimestamp(Timestamp.newBuilder()
//                        .setSeconds(1234)
//                        .setNanos(5234234)
//                ).setDeviceAdded(DeviceAddedEventProto.newBuilder()
//                        .setId("32")
//                        .setTypeValue(DeviceTypeProto.CLIMATE_SENSOR_VALUE)
//                        .build()
//                )
//                .build();
//        log.info("Отправляю данные 4: {}", deviceAddedEventProto.getAllFields());
//        collectorStub.collectHubEvent(deviceAddedEventProto);
    }
}
