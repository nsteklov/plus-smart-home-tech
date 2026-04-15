package ru.yandex.practicum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.NewDeliveryRequest;
import ru.yandex.practicum.commerce.dto.DeliveryDto;
import ru.yandex.practicum.commerce.dto.OrderDto;

@RestController
@RequestMapping(path = "/api/v1/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public DeliveryDto addDelivery(@Valid @RequestBody NewDeliveryRequest newDeliveryRequest) {
        log.info("PUT запрос на создание новой доставки: {} на склад", newDeliveryRequest);
        return deliveryService.addDelivery(newDeliveryRequest);
    }

    @PostMapping("/cost")
    @ResponseStatus(HttpStatus.OK)
    public Double cost(@Valid @RequestBody OrderDto orderDto) {
        log.info("POST запрос на расчет стоимости доставки заказа: {} на склад", orderDto);
        return deliveryService.cost(orderDto);
    }

    @PostMapping("/picked")
    @ResponseStatus(HttpStatus.OK)
    public DeliveryDto shipToDelivery(@RequestBody String orderId) {
        log.info("POST запрос на прием товаров по заказку с ИД: {} в доставку", orderId);
        return deliveryService.shipToDelivery(orderId);
    }

    @PostMapping("/successful")
    @ResponseStatus(HttpStatus.OK)
    public DeliveryDto successfulDelivery(@RequestBody String orderId) {
        log.info("POST запрос на доставка заказа с ИД: {}", orderId);
        return deliveryService.successfulDelivery(orderId);
    }

    @PostMapping("/failed")
    @ResponseStatus(HttpStatus.OK)
    public DeliveryDto deliveryFailed(@RequestBody String orderId) {
        log.info("POST неуспешная доставка заказа с ИД: {}", orderId);
        return deliveryService.deliveryFailed(orderId);
    }
}
