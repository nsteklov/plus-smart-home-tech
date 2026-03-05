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
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
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

@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    public EventController(Set<SensorEventHandler> sensorEventHandlers, Set<HubEventHandler> hubEventHandlers) {
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
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            // проверяем, есть ли обработчик для полученного события
            if (sensorEventHandlers.containsKey(request.getPayloadCase())) {
                // если обработчик найден, передаём событие ему на обработку
                sensorEventHandlers.get(request.getPayloadCase()).handle(request);
            } else {
                throw new IllegalArgumentException("Не могу найти обработчик для события " + request.getPayloadCase());
            }

            // после обработки события возвращаем ответ клиенту
            responseObserver.onNext(Empty.getDefaultInstance());
            // и завершаем обработку запроса
            responseObserver.onCompleted();
        } catch (Exception e) {
            // в случае исключения отправляем ошибку клиенту
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}
