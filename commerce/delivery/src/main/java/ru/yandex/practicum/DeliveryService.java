package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.*;
import ru.yandex.practicum.commerce.feign.OrderClient;
import ru.yandex.practicum.commerce.feign.WarehouseClient;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.model.DeliveryState;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;

    @Transactional
    public DeliveryDto addDelivery(NewDeliveryRequest newDeliveryRequest) {
        log.info("Создание новой доставки: {}", newDeliveryRequest);

        UUID orderUuid;
        try {
            orderUuid = UUID.fromString(newDeliveryRequest.getOrderId().replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + newDeliveryRequest.getOrderId());
        }
        Delivery delivery = DeliveryMapper.fromNewDeliveryRequestToEntity(newDeliveryRequest, orderUuid);
        delivery.setState(DeliveryState.CREATED);
        Delivery savedDelivery = deliveryRepository.save(delivery);
        log.info("Создана доставка: {}", savedDelivery);

        return DeliveryMapper.toDto(savedDelivery);
    }

    public Double cost(OrderDto orderDto) {
        log.info("Расчет стоимости доставки по заказу: {}", orderDto);

        UUID deliveryUuid;
        try {
            deliveryUuid = UUID.fromString(orderDto.getDeliveryId().replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID доставки " + orderDto.getDeliveryId());
        }
        Delivery delivery = deliveryRepository.findById(deliveryUuid)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена", HttpStatus.NOT_FOUND, "Доставка с UUID " + orderDto.getDeliveryId() + " не найдена"));
        Address deliveryAddress = delivery.getToAddress();
        Address warehouseAddress = delivery.getFromAddress();
        String warehouseAddressName = delivery.getWarehouseAddressName();
        Double baseCost = 5.0;
        Double cost = ((baseCost + baseCost * (warehouseAddressName.equals("ADDRESS_1") ? 1 : 2))
                * (1 + (orderDto.isFragile() ? 0.2 : 0))
                + (orderDto.getDeliveryWeight() == null ? 0 : orderDto.getDeliveryWeight() * 0.3)
                + (orderDto.getDeliveryVolume() == null ? 0 : orderDto.getDeliveryVolume() * 0.2))
                * (1 + ((warehouseAddress.getCountry().equals(deliveryAddress.getCountry())
                    && warehouseAddress.getCity().equals(deliveryAddress.getCity())
                    && warehouseAddress.getStreet().equals(deliveryAddress.getStreet())) ? 0 : 0.2));

        log.info("Рассчитана итоговая стоимость доставки: {}", cost);
        return cost;
    }

    @Transactional
    public DeliveryDto shipToDelivery(String orderUuidString) {
        log.info("Прием товаров по заказку с ид {} в доставку", orderUuidString);

        UUID orderUuid;
        try {
            orderUuid = UUID.fromString(orderUuidString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderUuidString);
        }
        Delivery delivery = deliveryRepository.findByOrderId(orderUuid)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена", HttpStatus.NOT_FOUND, "Доставка с UUID " + orderUuidString + " не обнаружена"));
        ShippedToDeliveryRequest  shippedToDeliveryRequest = new ShippedToDeliveryRequest();
        shippedToDeliveryRequest.setOrderId(delivery.getOrderId().toString());
        shippedToDeliveryRequest.setDeliveryId(delivery.getDeliveryId().toString());
        warehouseClient.shipToDelivery(shippedToDeliveryRequest);
        delivery.setState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);
        log.info("Товары по заказку с ид {} приняты в доставку", orderUuidString);

        return DeliveryMapper.toDto(delivery);
    }

    @Transactional
    public DeliveryDto successfulDelivery(String orderUuidString) {
        log.info("Прием товаров по заказку с ид {} в доставку", orderUuidString);

        UUID orderUuid;
        try {
            orderUuid = UUID.fromString(orderUuidString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderUuidString);
        }
        Delivery delivery = deliveryRepository.findByOrderId(orderUuid)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена", HttpStatus.NOT_FOUND, "Доставка с UUID " + orderUuidString + " не обнаружена"));
        orderClient.delivery(orderUuidString);
        delivery.setState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        log.info("Товары по заказку с ид {} успешно доставлены", orderUuidString);

        return DeliveryMapper.toDto(delivery);
    }

    @Transactional
    public DeliveryDto deliveryFailed(String orderUuidString) {
        log.info("Ошибка доставки товаров по заказу с ид {}", orderUuidString);

        UUID orderUuid;
        try {
            orderUuid = UUID.fromString(orderUuidString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderUuidString);
        }
        Delivery delivery = deliveryRepository.findByOrderId(orderUuid)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена", HttpStatus.NOT_FOUND, "Доставка с UUID " + orderUuidString + " не обнаружена"));
        orderClient.deliveryFailed(orderUuidString);
        delivery.setState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        log.info("Товары по заказку с ид {} не доставлены по ошибке", orderUuidString);

        return DeliveryMapper.toDto(delivery);
    }
}
