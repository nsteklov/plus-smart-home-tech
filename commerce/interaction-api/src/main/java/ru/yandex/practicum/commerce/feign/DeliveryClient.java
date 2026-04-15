package ru.yandex.practicum.commerce.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.*;

@FeignClient(name = "delivery")
public interface DeliveryClient {

    @PutMapping("/api/v1/delivery")
    DeliveryDto addDelivery(@RequestBody NewDeliveryRequest newDeliveryRequest);

    @PostMapping("/api/v1/delivery/cost")
    Double cost(@RequestBody OrderDto orderDto);
}
