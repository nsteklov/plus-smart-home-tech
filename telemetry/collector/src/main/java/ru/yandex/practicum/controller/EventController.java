package ru.yandex.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerOuterClass;
import ru.yandex.practicum.model.HubEvent;
import ru.yandex.practicum.model.HubEventType;
import ru.yandex.practicum.model.SensorEvent;
import ru.yandex.practicum.model.SensorEventType;
import ru.yandex.practicum.service.hub.HubEventHandler;
import ru.yandex.practicum.service.sensor.SensorEventHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Validated
@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubEventHandler> hubEventHandlers;

    public EventController(List<SensorEventHandler> sensorEventHandlers, Set<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
    }

//    @PostMapping("/sensors")
//    public void collectSensorEvent(@RequestBody @Valid SensorEvent request) {
//        log.info("json: {}", request.toString());
//        SensorEventHandler sensorEventHandler = sensorEventHandlers.get(request.getType());
//        if (sensorEventHandler == null) {
//            throw new IllegalArgumentException("Не найден обработчик для события: " + request.getType());
//        }
//        sensorEventHandler.handle(request);
//    }
//
//    @PostMapping("/hubs")
//    public void collectHubEvent(@RequestBody @Valid HubEvent request) {
//        log.info("json: {}", request.toString());
//        HubEventHandler hubEventHandler = hubEventHandlers.get(request.getType());
//        if (hubEventHandler == null) {
//            throw new IllegalArgumentException("Не найден обработчик для события: " + request.getType());
//        }
//        hubEventHandler.handle(request);
//    }

    @Override
    public void collectSensorEvent(ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerOuterClass.SensorEventProto request, io.grpc.stub.StreamObserver<ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerOuterClass.CollectorResponse> responseObserver) {
        try {
            // здесь реализуется бизнес-логика
            // ...

            responseObserver.onNext(CollectorControllerOuterClass.CollectorResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
