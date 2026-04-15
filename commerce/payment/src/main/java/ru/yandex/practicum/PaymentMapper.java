package ru.yandex.practicum;

import ru.yandex.practicum.commerce.dto.PaymentDto;
import ru.yandex.practicum.model.Payment;

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
