package ru.yandex.practicum;

import ru.yandex.practicum.commerce.dto.AddressDto;
import ru.yandex.practicum.commerce.dto.DeliveryDto;
import ru.yandex.practicum.commerce.dto.NewDeliveryRequest;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;

import java.util.UUID;

public class DeliveryMapper {

    public static Delivery fromNewDeliveryRequestToEntity(NewDeliveryRequest newDeliveryRequest,  UUID orderUuid) {
        Delivery delivery = new Delivery();
        delivery.setVolume(newDeliveryRequest.getVolume());
        delivery.setWeight(newDeliveryRequest.getWeight());
        delivery.setFragile(newDeliveryRequest.isFragile());
        if (newDeliveryRequest.getFromAddress() != null) {
            AddressDto addressFromDto = newDeliveryRequest.getFromAddress();
            Address addressFrom = new Address();
            addressFrom.setCountry(addressFromDto.getCountry());
            addressFrom.setCity(addressFromDto.getCity());
            addressFrom.setStreet(addressFromDto.getStreet());
            addressFrom.setHouse(addressFromDto.getHouse());
            addressFrom.setFlat(addressFromDto.getFlat());
            delivery.setFromAddress(addressFrom);
        }
        if (newDeliveryRequest.getToAddress() != null) {
            AddressDto addressToDto = newDeliveryRequest.getToAddress();
            Address addressTo = new Address();
            addressTo.setCountry(addressToDto.getCountry());
            addressTo.setCity(addressToDto.getCity());
            addressTo.setStreet(addressToDto.getStreet());
            addressTo.setHouse(addressToDto.getHouse());
            addressTo.setFlat(addressToDto.getFlat());
            delivery.setToAddress(addressTo);
        }
        delivery.setWarehouseAddressName(newDeliveryRequest.getWarehouseAddressName());
        delivery.setOrderId(orderUuid);

        return delivery;
    }

    public static DeliveryDto toDto(Delivery delivery) {
        DeliveryDto deliveryDto = new DeliveryDto();
        if (delivery.getDeliveryId() != null) {
            deliveryDto.setDeliveryId(delivery.getDeliveryId().toString());
        }
        if (delivery.getFromAddress() != null) {
            Address addressFrom = delivery.getFromAddress();
            AddressDto addressFromDto = new AddressDto();
            addressFromDto.setCountry(addressFrom.getCountry());
            addressFromDto.setCity(addressFrom.getCity());
            addressFromDto.setStreet(addressFrom.getStreet());
            addressFromDto.setHouse(addressFrom.getHouse());
            addressFromDto.setFlat(addressFrom.getFlat());
            deliveryDto.setFromAddress(addressFromDto);
        }
        if (delivery.getToAddress() != null) {
            Address addressTo = delivery.getToAddress();
            AddressDto addressToDto = new AddressDto();
            addressToDto.setCountry(addressTo.getCountry());
            addressToDto.setCity(addressTo.getCity());
            addressToDto.setStreet(addressTo.getStreet());
            addressToDto.setHouse(addressTo.getHouse());
            addressToDto.setFlat(addressTo.getFlat());
            deliveryDto.setToAddress(addressToDto);
        }
        if (delivery.getOrderId() != null) {
            deliveryDto.setOrderId(delivery.getOrderId().toString());
        }
        if (delivery.getState() != null) {
            deliveryDto.setDeliveryState(delivery.getState().toString());
        }

        return deliveryDto;
    }
}
