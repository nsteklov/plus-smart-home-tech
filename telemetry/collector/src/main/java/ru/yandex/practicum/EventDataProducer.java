package ru.yandex.practicum;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.*;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class EventDataProducer {

    @GrpcClient("collector")
    private CollectorControllerGrpc.CollectorControllerBlockingStub collectorStub;

    public void sendEvents() {

        //DEVICE_ADDED
        HubEventProto deviceAddedEventProto = HubEventProto.newBuilder()
                .setHubId("123")
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(1234)
                        .setNanos(5234234)
                ).setDeviceAdded(DeviceAddedEventProto.newBuilder()
                        .setId("1")
                        .setType(DeviceTypeProto.MOTION_SENSOR)
                        .build()
                )
                .build();
        log.info("Отправляю данные 4: {}", deviceAddedEventProto.getAllFields());
        collectorStub.collectHubEvent(deviceAddedEventProto);

        //SCENARIO_ADDED
        List<ScenarioConditionProto> scenarioConditionsProto = new ArrayList<>();
        ScenarioConditionProto scenarioConditionProto = ScenarioConditionProto.newBuilder()
                .setSensorId("1")
                .setType(ConditionTypeProto.CO2LEVEL)
                .setOperation(ConditionOperationProto.EQUALS)
                .setIntValue(432)
                .build();
        scenarioConditionsProto.add(scenarioConditionProto);

        List<DeviceActionProto> deviceActionsProto = new ArrayList<>();
        DeviceActionProto deviceActionProto = DeviceActionProto.newBuilder()
                .setSensorId("1")
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
                                .build()
                )
                .build();
        log.info("Отправляю данные 5: {}", scenarioAddedEventProto.getAllFields());
        collectorStub.collectHubEvent(scenarioAddedEventProto);

        //Событие климатического датчика
        SensorEventProto climateSensorProto = SensorEventProto.newBuilder()
                .setId("1")
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(2612411)
                        .setNanos(5234239)
                ).setClimateSensor(
                        ClimateSensorProto.newBuilder()
                                .setTemperatureC(12397)
                                .setHumidity(321)
                                .setCo2Level(432)
                                .build()
                )
                .setHubId("123")
                .build();
        log.info("Отправляю данные 2: {}", climateSensorProto.getAllFields());
        collectorStub.collectSensorEvent(climateSensorProto);

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
