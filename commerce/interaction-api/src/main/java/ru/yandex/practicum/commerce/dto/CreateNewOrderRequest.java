package ru.yandex.practicum.commerce.dto;

import lombok.Getter;

@Getter
public class CreateNewOrderRequest {

    private ShoppingCartDto shoppingCart;
    private  AddressDto deliveryAddress;
}
