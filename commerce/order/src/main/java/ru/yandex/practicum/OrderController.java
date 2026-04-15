package ru.yandex.practicum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.*;

@RestController
@RequestMapping(path = "/api/v1/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<OrderDto> getOrdersByUserName(@RequestParam String username) {
        log.info("GET запрос на получение заказов по имени пользователя {}", username);
        return orderService.getOrdersByUserName(username);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest createNewOrderRequest) {
        log.info("PUT запрос на создание нового заказа: {} на склад", createNewOrderRequest);
        return orderService.createNewOrder(createNewOrderRequest);
    }

    @PostMapping("/assembly")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto assembly(@RequestBody String orderId) {
        log.info("POST запрос на сборку заказа с id: {} на склад", orderId);
        return orderService.assembly(orderId);
    }

    @PostMapping("/assembly/failed")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto assemblyFailed(@RequestBody String orderId) {
        log.info("POST запрос ошибка сборки заказа с id: {}", orderId);
        return orderService.assemblyFailed(orderId);
    }

    @PostMapping("/return")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto returnOrder(@RequestBody ProductReturnRequest productReturnRequest) {
        log.info("POST запрос на возврат заказа: {}", productReturnRequest);
        return orderService.returnOrder(productReturnRequest);
    }

    @PostMapping("/delivery/plan")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto deliveryPlan(@RequestBody String orderId) {
        log.info("POST запрос на создание доставки заказа с id: {}", orderId);
        return orderService.deliveryPlan(orderId);
    }

    @PostMapping("/delivery")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto delivery(@RequestBody String orderId) {
        log.info("POST запрос на успешную доставку заказа с id: {}", orderId);
        return orderService.delivery(orderId);
    }

    @PostMapping("/delivery/failed")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto deliveryFailed(@RequestBody String orderId) {
        log.info("POST запрос ошибка доставки заказа с id: {}", orderId);
        return orderService.deliveryFailed(orderId);
    }

    @PostMapping("/calculate/delivery")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto calculateDelivery(@RequestBody String orderId) {
        log.info("POST запрос на расчет стоимости доставки заказа с id: {}", orderId);
        return orderService.calculateDelivery(orderId);
    }

    @PostMapping("/calculate/product")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto calculateProductCost(@RequestBody String orderId) {
        log.info("POST запрос на расчет стоимости товаров по заказу с id: {}", orderId);
        return orderService.calculateProductCost(orderId);
    }

    @PostMapping("/calculate/total")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto calculateTotalCost(@RequestBody String orderId) {
        log.info("POST запрос на расчет итоговой стоимости заказа с id: {}", orderId);
        return orderService.calculateTotalCost(orderId);
    }

    @PostMapping("/payment")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto payment(@RequestBody String orderId) {
        log.info("POST запрос на оплату заказа с id: {}", orderId);
        return orderService.payment(orderId);
    }

    @PostMapping("/payment/success")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto paymentSuccess(@RequestBody String orderId) {
        log.info("POST запрос на проставление признака успешной оплаты заказа с id: {}", orderId);
        return orderService.paymentSuccess(orderId);
    }

    @PostMapping("/payment/failed")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto paymentFailed(@RequestBody String orderId) {
        log.info("POST запрос на проставление признака неуспешной оплаты заказа с id: {}", orderId);
        return orderService.paymentFailed(orderId);
    }

    @PostMapping("/completed")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto completeOrder(@RequestBody String orderId) {
        log.info("POST запрос на завершение заказа с id: {}", orderId);
        return orderService.completeOrder(orderId);
    }
}
