package ru.yandex.practicum.commerce.dto;

import lombok.Data;
import java.util.Map;

@Data
public class OrderDto {

    private String orderId;
    private String shoppingCartId;
    private String paymentId;
    private String deliveryId;
    private String state;
    private Double deliveryWeight;
    private Double deliveryVolume;
    public boolean fragile;
    private Double totalPrice;
    private Double deliveryPrice;
    private Double productPrice;
    private Map<String, Integer> products;
}
