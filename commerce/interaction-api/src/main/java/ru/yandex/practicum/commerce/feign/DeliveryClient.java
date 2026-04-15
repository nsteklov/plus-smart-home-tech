package ru.yandex.practicum.commerce.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.*;

import java.util.Map;

@FeignClient(name = "delivery")
public interface DeliveryClient {

    @PutMapping("/api/v1/delivery")
    DeliveryDto addDelivery(@RequestBody NewDeliveryRequest newDeliveryRequest);

    @PostMapping("/api/v1/delivery/cost")
    Double cost(@RequestBody OrderDto orderDto);
}
