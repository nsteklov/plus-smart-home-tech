package ru.yandex.practicum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.OrderDto;
import ru.yandex.practicum.commerce.dto.PaymentDto;

@RestController
@RequestMapping(path = "/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/productCost")
    @ResponseStatus(HttpStatus.OK)
    public Double productCost(@Valid @RequestBody OrderDto orderDto) {
        log.info("POST запрос на расчет стоимости товаров в заказе: {}", orderDto);
        return paymentService.productCost(orderDto);
    }

    @PostMapping("/totalCost")
    @ResponseStatus(HttpStatus.OK)
    public Double totalCost(@Valid @RequestBody OrderDto orderDto) {
        log.info("POST запрос на расчет итоговой стоимости заказа: {}", orderDto);
        return paymentService.totalCost(orderDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public PaymentDto payment(@Valid @RequestBody OrderDto orderDto) {
        log.info("POST запрос на создание оплаты заказа: {}", orderDto);
        return paymentService.payment(orderDto);
    }

    @PostMapping("/refund")
    @ResponseStatus(HttpStatus.OK)
    public PaymentDto paymentRefund(@RequestBody String paymentId) {
        log.info("POST запрос на проставление признака успешной оплаты с id: {}", paymentId);
        return paymentService.paymentRefund(paymentId);
    }

    @PostMapping("/failed")
    @ResponseStatus(HttpStatus.OK)
    public PaymentDto paymentFailed(@RequestBody String paymentId) {
        log.info("POST запрос на проставление признака неуспешной оплаты с id: {}", paymentId);
        return paymentService.paymentFailed(paymentId);
    }
}
