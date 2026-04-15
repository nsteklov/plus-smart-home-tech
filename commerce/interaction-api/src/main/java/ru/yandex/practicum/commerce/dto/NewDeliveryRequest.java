package ru.yandex.practicum.commerce.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class NewDeliveryRequest {

    private String orderId;
    private Double volume;
    private Double weight;
    private boolean fragile;
    private String warehouseAddressName;
    private AddressDto fromAddress;
    private AddressDto toAddress;
}
