package ru.yandex.practicum;

import ru.yandex.practicum.commerce.dto.ShoppingCartDto;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShoppingCartMapper {

    public static ShoppingCartDto toDto(ShoppingCart shoppingCart) {

        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        Map<String, Integer> productsDto = new HashMap<>();
        for (Map.Entry<UUID, Integer> entry : shoppingCart.getProducts().entrySet()) {
            productsDto.put(entry.getKey().toString(), entry.getValue());
        }
        shoppingCartDto.setShoppingCartId(shoppingCart.getShoppingCartId().toString());
        shoppingCartDto.setProducts(productsDto);
        return shoppingCartDto;
    }
}
