package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.*;
import ru.yandex.practicum.commerce.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.feign.*;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.exception.NotOrderFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final ShoppingCartClient shoppingCartClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;

    public Page<OrderDto> getOrdersByUserName (String username) {
        log.info("Получение заказов по пользователю с именем: {}", username);

        if (username == null || username.isEmpty()) {
            throw new NotAuthorizedUserException("Не задано имя пользователя", HttpStatus.UNAUTHORIZED, "Передано пустое имя пользователя");
        }
        Pageable pageable = PageRequest.of(0, 20);
        Page<Order> orderPage = orderRepository.findByUsername(username, pageable);
        log.info("Получен список заказов по пользователю с именем: {}", username);
        return orderPage.map(order -> OrderMapper.toDto(order));
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest createNewOrderRequest) {
        log.info("Создание нового заказа: {}", createNewOrderRequest);

        UUID shoppingCartUuid;
        try {
            shoppingCartUuid = UUID.fromString(createNewOrderRequest.getShoppingCart().getShoppingCartId().replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID корзины покупателя" + createNewOrderRequest.getShoppingCart().getShoppingCartId());
        }

        ShoppingCartDto shoppingCartDto = createNewOrderRequest.getShoppingCart();
        try {
            BookedProductsDto bookedProductsDto = warehouseClient.checkProductsInWarehouse(shoppingCartDto);
        } catch (ProductInShoppingCartLowQuantityInWarehouse productInShoppingCartLowQuantityInWarehouse) {
            throw new NoSpecifiedProductInWarehouseException(productInShoppingCartLowQuantityInWarehouse.getMessage(), productInShoppingCartLowQuantityInWarehouse.getHttpStatus(), productInShoppingCartLowQuantityInWarehouse.getUserMessage());
        }

        Map<UUID, Integer> savedProducts = new HashMap<>();
        for (Map.Entry<String, Integer> entry : shoppingCartDto.getProducts().entrySet()) {
            try {
                UUID uuid = UUID.fromString(entry.getKey().replace("\"", ""));
                savedProducts.put(uuid, entry.getValue());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Передан некорректный формат UUID " + entry.getKey());
            }
        }
        String username = shoppingCartClient.getUserNameByProductCartUUID(shoppingCartDto.getShoppingCartId());

        Order order = new Order();
        order.setShoppingCartId(shoppingCartUuid);
        order.setUsername(username);
        order.setState(OrderState.NEW);
        order.setProducts(savedProducts);
        if (createNewOrderRequest.getDeliveryAddress() != null) {
            AddressDto addressToDto = createNewOrderRequest.getDeliveryAddress();
            Address addressTo = new Address();
            addressTo.setCountry(addressToDto.getCountry());
            addressTo.setCity(addressToDto.getCity());
            addressTo.setStreet(addressToDto.getStreet());
            addressTo.setHouse(addressToDto.getHouse());
            addressTo.setFlat(addressToDto.getFlat());
            order.setDeliveryAddress(addressTo);
        }
        WarehouseAddressDto warehouseAddressDto = warehouseClient.getWarehouseAddress();
        if (warehouseAddressDto!= null) {
            Address addressFrom = new Address();
            addressFrom.setCountry(warehouseAddressDto.getCountry());
            addressFrom.setCity(warehouseAddressDto.getCity());
            addressFrom.setStreet(warehouseAddressDto.getStreet());
            addressFrom.setHouse(warehouseAddressDto.getHouse());
            addressFrom.setFlat(warehouseAddressDto.getFlat());
            order.setWarehouseAddress(addressFrom);
            order.setWarehouseAddressName(warehouseAddressDto.getName());
        }
        Order savedOrder = orderRepository.save(order);
        log.info("Заказ создан: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto assembly(String orderIdString) {
        log.info("Сборка заказа с id: {}", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        AssemblyProductsForOrderRequest assemblyProductsForOrderRequest =  new AssemblyProductsForOrderRequest();
        assemblyProductsForOrderRequest.setOrderId(orderIdString);
        Map<String, Integer> productsToAssembly = order.getProducts().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        Map.Entry::getValue
                ));
        assemblyProductsForOrderRequest.setProducts(productsToAssembly);
        BookedProductsDto bookedProductsDto = warehouseClient.assembly(assemblyProductsForOrderRequest);
        order.setDeliveryWeight(bookedProductsDto.getDeliveryWeight());
        order.setDeliveryVolume(bookedProductsDto.getDeliveryVolume());
        order.setFragile(bookedProductsDto.isFragile());
        order.setState(OrderState.ASSEMBLED);
        Order savedOrder = orderRepository.save(order);
        log.info("Заказ собран: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto assemblyFailed(String orderIdString) {
        log.info("Сборка заказа с id: {} с ошибкой", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        order.setState(OrderState.ASSEMBLY_FAILED);
        Order savedOrder = orderRepository.save(order);
        log.info("Заказ: {} собран с ошибкой", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto returnOrder(ProductReturnRequest productReturnRequest) {
        log.info("Возврат заказа: {}", productReturnRequest);

        String orderIdString = productReturnRequest.getOrderId().replace("\"", "");
        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        Map<String, Integer> productsToReturn = productReturnRequest.getProducts().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        Map.Entry::getValue
                ));
        warehouseClient.returnProducts(productsToReturn);
        order.setState(OrderState.PRODUCT_RETURNED);
        Order savedOrder = orderRepository.save(order);
        log.info("Заказ возвращен: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto deliveryPlan(String orderIdString) {
        log.info("Создание доставки для заказа с id: {}", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        NewDeliveryRequest newDeliveryRequest =  new NewDeliveryRequest();
        newDeliveryRequest.setOrderId(orderIdString);
        newDeliveryRequest.setVolume(order.getDeliveryVolume());
        newDeliveryRequest.setWeight(order.getDeliveryWeight());
        newDeliveryRequest.setFragile(order.isFragile());
        if (order.getDeliveryAddress() != null) {
            Address addressTo = order.getDeliveryAddress();
            AddressDto addressToDto = new AddressDto();
            addressToDto.setCountry(addressTo.getCountry());
            addressToDto.setCity(addressTo.getCity());
            addressToDto.setStreet(addressTo.getStreet());
            addressToDto.setHouse(addressTo.getHouse());
            addressToDto.setFlat(addressTo.getFlat());
            newDeliveryRequest.setToAddress(addressToDto);
        }
        if (order.getWarehouseAddress() != null) {
            Address addressFrom = order.getWarehouseAddress();
            AddressDto addressFromDto = new AddressDto();
            addressFromDto.setCountry(addressFrom.getCountry());
            addressFromDto.setCity(addressFrom.getCity());
            addressFromDto.setStreet(addressFrom.getStreet());
            addressFromDto.setHouse(addressFrom.getHouse());
            addressFromDto.setFlat(addressFrom.getFlat());
            newDeliveryRequest.setFromAddress(addressFromDto);
        }
        newDeliveryRequest.setWarehouseAddressName(order.getWarehouseAddressName());
        DeliveryDto deliveryDto = deliveryClient.addDelivery(newDeliveryRequest);
        order.setState(OrderState.ON_DELIVERY);
        UUID deliveryId;
        try {
            deliveryId = UUID.fromString(deliveryDto.getDeliveryId().replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID доставки " + deliveryDto.getDeliveryId());
        }
        order.setDeliveryId(deliveryId);
        Order savedOrder = orderRepository.save(order);
        log.info("Создана доставка {} для заказа с id: {}", deliveryDto, orderIdString);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto calculateDelivery(String orderIdString) {
        log.info("Расчето стоимости доставки для заказа с id: {}", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        OrderDto orderDto = OrderMapper.toDto(order);
        Double deliveryCost = deliveryClient.cost(orderDto);
        order.setDeliveryPrice(deliveryCost);
        Order savedOrder = orderRepository.save(order);
        log.info("Рассчитана стоимость доставки для заказа: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto calculateProductCost(String orderIdString) {
        log.info("Расчет стоимости товаров для заказа с id: {}", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        OrderDto orderDto = OrderMapper.toDto(order);
        Double productCost = paymentClient.productCost(orderDto);
        order.setProductPrice(productCost);
        Order savedOrder = orderRepository.save(order);
        log.info("Рассчитана стоимость товаров для заказа: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto calculateTotalCost(String orderIdString) {
        log.info("Расчет итоговой стоимости заказа с id: {}", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        OrderDto orderDto = OrderMapper.toDto(order);
        Double totalCost = paymentClient.totalCost(orderDto);
        order.setTotalPrice(totalCost);
        Order savedOrder = orderRepository.save(order);
        log.info("Рассчитана итоговая стоимость заказа: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto payment(String orderIdString) {
        log.info("Создание оплаты заказа с id: {}", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        OrderDto orderDto = OrderMapper.toDto(order);
        PaymentDto paymentDto = paymentClient.payment(orderDto);
        UUID paymentId;
        try {
            paymentId = UUID.fromString(paymentDto.getPaymentId().replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID оплаты заказа " + orderIdString);
        }
        order.setPaymentId(paymentId);
        order.setState(OrderState.ON_PAYMENT);
        Order savedOrder = orderRepository.save(order);
        log.info("Создана оплата заказа: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto paymentSuccess(String orderIdString) {
        log.info("Проставление признака успешной оплаты заказа с id: {}", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        order.setState(OrderState.PAID);
        Order savedOrder = orderRepository.save(order);
        log.info("Проставлен признак успешной оплаты заказа: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto paymentFailed(String orderIdString) {
        log.info("Проставление признака неуспешной оплаты заказа с id: {}", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        order.setState(OrderState.PAYMENT_FAILED);
        Order savedOrder = orderRepository.save(order);
        log.info("Проставлен признак неуспешной оплаты заказа: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto delivery(String orderIdString) {
        log.info("Доставка заказа с id: {}", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        order.setState(OrderState.DELIVERED);
        Order savedOrder = orderRepository.save(order);
        log.info("Успешная доставка заказа: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto deliveryFailed(String orderIdString) {
        log.info("Доставка заказа с id: {} с ошибкой", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        order.setState(OrderState.DELIVERY_FAILED);
        Order savedOrder = orderRepository.save(order);
        log.info("Ошибка доставки заказа: {}", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto completeOrder(String orderIdString) {
        log.info("Завершение заказа с id: {}", orderIdString);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderIdString);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotOrderFoundException("Заказ не найден", HttpStatus.BAD_REQUEST, "Заказа с UUID " + orderIdString + " не найден"));
        order.setState(OrderState.COMPLETED);
        Order savedOrder = orderRepository.save(order);
        log.info("Заказ с id: {} успешно завершен", savedOrder);

        return OrderMapper.toDto(savedOrder);
    }
}
