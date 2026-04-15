package ru.yandex.practicum.commerce.dto;

import lombok.Data;

@Data
public class PaymentDto {

    private String paymentId;
    private Double totalPayment;
    private Double deliveryTotal;
    private Double feeTotal;
}
