package ru.yandex.practicum.commerce.dto;

import lombok.Data;

@Data
public class DeliveryDto {

    private String deliveryId;
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private String orderId;
    private String deliveryState;
}
