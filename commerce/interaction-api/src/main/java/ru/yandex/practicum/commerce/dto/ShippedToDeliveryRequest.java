package ru.yandex.practicum.commerce.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ShippedToDeliveryRequest {

    private String orderId;
    private String deliveryId;
}
