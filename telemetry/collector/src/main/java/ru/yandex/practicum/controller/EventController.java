package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

@RestController
@Slf4j
@Validated
@RequestMapping(path = "/events")
public class EventController {
    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubEventHandler> hubEventHandlers;

    public EventController(List<SensorEventHandler> sensorEventHandlers, Set<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
    }

    @PostMapping("/sensors")
    public void collectSensorEvent(@RequestBody @Valid SensorEvent request) {
        log.info("json: {}", request.toString());
        SensorEventHandler sensorEventHandler = sensorEventHandlers.get(request.getType());
        if (sensorEventHandler == null) {
            throw new IllegalArgumentException("Не найден обработчик для события: " + request.getType());
        }
        sensorEventHandler.handle(request);
    }

    @PostMapping("/hubs")
    public void collectHubEvent(@RequestBody @Valid HubEvent request) {
        log.info("json: {}", request.toString());
        HubEventHandler hubEventHandler = hubEventHandlers.get(request.getType());
        if (hubEventHandler == null) {
            throw new IllegalArgumentException("Не найден обработчик для события: " + request.getType());
        }
        hubEventHandler.handle(request);
    }
}
