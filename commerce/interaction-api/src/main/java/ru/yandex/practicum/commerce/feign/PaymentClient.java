package ru.yandex.practicum.commerce.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.dto.OrderDto;
import ru.yandex.practicum.commerce.dto.PaymentDto;
import ru.yandex.practicum.commerce.dto.ProductDto;

import java.util.List;

@FeignClient(name = "payment")
public interface PaymentClient {

    @PostMapping("/api/v1/payment/productCost")
    Double productCost(@RequestBody OrderDto orderDto);

    @PostMapping("/api/v1/payment/totalCost")
    Double totalCost(@RequestBody OrderDto orderDto);

    @PostMapping("/api/v1/payment")
    PaymentDto payment(@RequestBody OrderDto orderDto);
}
