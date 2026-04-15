package ru.yandex.practicum;

import ru.yandex.practicum.commerce.dto.OrderDto;
import ru.yandex.practicum.commerce.dto.ShoppingCartDto;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrderMapper {

    public static OrderDto toDto(Order order) {

        OrderDto orderDto = new OrderDto();
        Map<String, Integer> productsDto = new HashMap<>();
        for (Map.Entry<UUID, Integer> entry : order.getProducts().entrySet()) {
            productsDto.put(entry.getKey().toString(), entry.getValue());
        }
        if (order.getOrderId() != null) {
            orderDto.setOrderId(order.getOrderId().toString());
        }
        if (order.getShoppingCartId() != null) {
            orderDto.setShoppingCartId(order.getShoppingCartId().toString());
        }
        if (order.getPaymentId() != null) {
            orderDto.setPaymentId(order.getPaymentId().toString());
        }
        if (order.getDeliveryId() != null) {
            orderDto.setDeliveryId(order.getDeliveryId().toString());
        }
        if (order.getState() != null) {
            orderDto.setState(order.getState().toString());
        }
        orderDto.setDeliveryWeight(order.getDeliveryWeight());
        orderDto.setDeliveryVolume(order.getDeliveryVolume());
        orderDto.setFragile(order.isFragile());
        orderDto.setTotalPrice(order.getTotalPrice());
        orderDto.setDeliveryPrice(order.getDeliveryPrice());
        orderDto.setProductPrice(order.getProductPrice());
        orderDto.setProducts(productsDto);
        return orderDto;
    }
}
