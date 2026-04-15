package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.OrderDto;
import ru.yandex.practicum.commerce.dto.PaymentDto;
import ru.yandex.practicum.commerce.dto.ProductDto;
import ru.yandex.practicum.commerce.feign.OrderClient;
import ru.yandex.practicum.commerce.feign.ShoppingStoreClient;
import ru.yandex.practicum.commerce.feign.WarehouseClient;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.model.PaymentState;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    public Double productCost(OrderDto orderDto) {
        log.info("Расчет стоимости товаров в заказе: {}", orderDto);

        List<String> productIds = orderDto.getProducts().keySet().stream()
                .collect(Collectors.toList());
        String[] productIdsArray = productIds.toArray(new String[productIds.size()]);
        System.out.println(Arrays.toString(productIdsArray));
        List<ProductDto> productsInShoppingStoreDto = shoppingStoreClient.getProductsByIds(productIdsArray);
        Map<String, ProductDto> productsInShoppingStoreDtoMap = productsInShoppingStoreDto.stream()
                .collect(Collectors.toMap(
                        ProductDto::getProductId,
                        Function.identity()
                ));

        double productCost = 0.0;
        for (Map.Entry<String, Integer> entry : orderDto.getProducts().entrySet()) {
            if (productsInShoppingStoreDtoMap.containsKey(entry.getKey())
                    && productsInShoppingStoreDtoMap.get(entry.getKey()).getPrice() != null
                    && productsInShoppingStoreDtoMap.get(entry.getKey()).getPrice() != 0) {
                productCost = productCost + entry.getValue() * productsInShoppingStoreDtoMap.get(entry.getKey()).getPrice();
            } else {
                throw new NotEnoughInfoInOrderToCalculateException("Не хватает информации для вычисления стоимости товаров", HttpStatus.BAD_REQUEST, "Для заказа " + orderDto + " недостаточно информации для вычисления стоимости товаров");
            }
        }

        log.info("Рассчитана стоимость товаров по заказу: {}", orderDto);
        return productCost;
    }

    public Double totalCost(OrderDto orderDto) {
        log.info("Расчет итоговой стоимости заказа: {}", orderDto);

        Double deliveryCost = orderDto.getDeliveryPrice();
        Double productCost = orderDto.getProductPrice();
        if (deliveryCost == null || deliveryCost == 0 || productCost == null || productCost == 0) {
            throw new NotEnoughInfoInOrderToCalculateException("Не хватает информации для вычисления итоговой стоимости заказа", HttpStatus.BAD_REQUEST, "Для заказа " + orderDto + " недостаточно информации для вычисления итоговой стоимости");
        }
        Double totalCost = productCost * 1.1 + deliveryCost;
        log.info("Рассчитана итоговая стоимость заказа: {}", orderDto);

        return totalCost;
    }

    @Transactional
    public PaymentDto payment(OrderDto orderDto) {
        log.info("Формирование оплаты заказа: {}", orderDto);

        Double deliveryCost = orderDto.getDeliveryPrice();
        Double productCost = orderDto.getProductPrice();
        Double totalCost = orderDto.getTotalPrice();
        if (deliveryCost == null || deliveryCost == 0 || productCost == null || productCost == 0|| totalCost == null || totalCost == 0) {
            throw new NotEnoughInfoInOrderToCalculateException("Не хватает информации для создания оплаты заказа", HttpStatus.BAD_REQUEST, "Для заказа " + orderDto + " недостаточно информации для формирования оплаты");
        }
        UUID orderId;
        try {
            orderId = UUID.fromString(orderDto.getOrderId().replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID заказа " + orderDto.getOrderId());
        }
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setProductsTotal(productCost);
        payment.setDeliveryTotal(deliveryCost);
        payment.setFeeTotal(totalCost - productCost - deliveryCost);
        payment.setState(PaymentState.PENDING);
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Создана оплата {} заказа: {}", savedPayment, orderDto);

        return PaymentMapper.toDto(savedPayment);
    }

    @Transactional
    public PaymentDto paymentRefund(String paymentIdString) {
        log.info("Проставление признака успешной оплаты с id: {}", paymentIdString);

        UUID paymentId;
        try {
            paymentId = UUID.fromString(paymentIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID оплаты " + paymentIdString);
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Оплата не найдена", HttpStatus.BAD_REQUEST, "Оплата с UUID " + paymentIdString + " не найдена"));
        if (payment.getOrderId() == null) {
            throw new NoOrderFoundException("Заказ для оплаты не найден", HttpStatus.BAD_REQUEST, "Заказ для оплаты с UUID " + paymentIdString + " не найден");
        }
        OrderDto orderDto = orderClient.paymentSuccess(payment.getOrderId().toString());
        payment.setState(PaymentState.SUCCESS);
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Поставлен признак успешной оплаты {} по заказу: {}", savedPayment, orderDto);

        return PaymentMapper.toDto(savedPayment);
    }

    @Transactional
    public PaymentDto paymentFailed(String paymentIdString) {
        log.info("Проставление признака неуспешной оплаты с id: {}", paymentIdString);

        UUID paymentId;
        try {
            paymentId = UUID.fromString(paymentIdString.replace("\"", ""));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID оплаты " + paymentIdString);
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Оплата не найдена", HttpStatus.BAD_REQUEST, "Оплата с UUID " + paymentIdString + " не найдена"));
        if (payment.getOrderId() == null) {
            throw new NoOrderFoundException("Заказ для оплаты не найден", HttpStatus.BAD_REQUEST, "Заказ для оплаты с UUID " + paymentIdString + " не найден");
        }
        OrderDto orderDto = orderClient.paymentFailed(payment.getOrderId().toString());
        payment.setState(PaymentState.FAILED);
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Поставлен признак неуспешной оплаты {} по заказу: {}", savedPayment, orderDto);

        return PaymentMapper.toDto(savedPayment);
    }
}
