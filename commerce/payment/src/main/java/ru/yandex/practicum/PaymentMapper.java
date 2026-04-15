package ru.yandex.practicum;

import ru.yandex.practicum.commerce.dto.PaymentDto;
import ru.yandex.practicum.commerce.dto.ShoppingCartDto;
import ru.yandex.practicum.model.Payment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaymentMapper {

    public static PaymentDto toDto(Payment payment) {

        PaymentDto paymentDto = new PaymentDto();
        if (payment.getPaymentId() != null) {
            paymentDto.setPaymentId(payment.getPaymentId().toString());
        }
        paymentDto.setTotalPayment(payment.getProductsTotal() + payment.getDeliveryTotal() + payment.getFeeTotal());
        paymentDto.setDeliveryTotal(payment.getDeliveryTotal());
        paymentDto.setFeeTotal(payment.getFeeTotal());
        return paymentDto;
    }
}
