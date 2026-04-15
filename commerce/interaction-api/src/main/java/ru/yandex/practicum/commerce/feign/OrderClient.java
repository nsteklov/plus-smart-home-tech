package ru.yandex.practicum.commerce.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.dto.OrderDto;

@FeignClient(name = "order")
public interface OrderClient {

    @PostMapping("/api/v1/order/delivery")
    OrderDto delivery(@RequestBody String orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    OrderDto deliveryFailed(@RequestBody String orderId);

    @PostMapping("/api/v1/order/payment/success")
    OrderDto paymentSuccess(@RequestBody String orderId);

    @PostMapping("/api/v1/order/payment/failed")
    OrderDto paymentFailed(@RequestBody String orderId);
}
