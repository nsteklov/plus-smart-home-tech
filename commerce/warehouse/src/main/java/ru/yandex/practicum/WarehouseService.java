package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.*;
import ru.yandex.practicum.commerce.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.model.ProductInWarehouse;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WarehouseService {

    private final ProductRepository productRepository;
    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};
    private static final Map<String, AddressDto> WAREHOUSE_DATA = new HashMap<>();
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    static {
        WAREHOUSE_DATA.put("ADDRESS_1", new AddressDto(
                "Россия",
                "Москва",
                "Бобруйская",
                "1",
                "1"
        ));

        WAREHOUSE_DATA.put("ADDRESS_2", new AddressDto(
                "Белоруссия",
                "Бобруйск",
                "Московска",
                "2",
                "2" // не имеет холодильных камер
        ));
    }

    @Transactional
    public boolean createProduct(NewProductWarehouseRequest newProductWarehouseRequest) {
        log.info("Добавление нового товара : {} на склад", newProductWarehouseRequest);

        UUID uuid;
        try {
            uuid = UUID.fromString(newProductWarehouseRequest.getProductId());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID " + newProductWarehouseRequest.getProductId());
        }
        if (productRepository.existsByProductId(uuid)) {
            throw new SpecifiedProductAlreadyInWarehouseException("На складе обнаружен товар с таким же описанием", HttpStatus.BAD_REQUEST, "Товар с таким описанием уже зарегистрирован на складе");
        }

        ProductInWarehouse product = ProductMapper.toEntity(newProductWarehouseRequest, uuid);
        ProductInWarehouse savedProduct = productRepository.save(product);
        log.info("Товар добавлен: {}", product);

        return true;
    }

    public BookedProductsDto checkProductsInWarehouse(ShoppingCartDto shoppingCartDto) {

        Map<UUID, Integer> productsInShoppingCart = shoppingCartDto.getProducts();
        List<UUID> productIds = shoppingCartDto.getProducts().entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        List<ProductInWarehouse> productsInWarehouse = productRepository.findByProductIds(productIds);
        Map<UUID, Integer> productsInWarehouseMap = productsInWarehouse.stream()
                .collect(Collectors.toMap(
                        ProductInWarehouse::getProductId,
                        ProductInWarehouse::getQuantity
                ));
        boolean lowQuantity = false;
        String errorMessage = "На складе не хватает товаров с UUID: ";
        for (Map.Entry<UUID, Integer> entry : productsInShoppingCart.entrySet()) {
            if (!productsInWarehouseMap.containsKey(entry.getKey()) || productsInWarehouseMap.get(entry.getKey()) < entry.getValue()) {
                errorMessage = errorMessage + entry.getKey() + ",";
                lowQuantity = true;
            }
        }
        if (lowQuantity) {
            errorMessage = errorMessage.substring(0, errorMessage.length() - 1);
            throw new ProductInShoppingCartLowQuantityInWarehouse("На складе не хватает товаров", HttpStatus.BAD_REQUEST, errorMessage);
        }
        Boolean fragile = false;
        Double deliveryWeight = 0.0;
        Double deliveryVolume = 0.0;
        for (ProductInWarehouse product : productsInWarehouse) {
            if (product.isFragile()) {
                fragile = true;
            }
            deliveryWeight = deliveryWeight + product.getWeight();
            deliveryVolume = deliveryVolume + (product.getDimension().getWidth() * product.getDimension().getDepth() * product.getDimension().getHeight());
        }
        BookedProductsDto bookedProductsDto = new BookedProductsDto();
        bookedProductsDto.setFragile(fragile);
        bookedProductsDto.setDeliveryWeight(deliveryWeight);
        bookedProductsDto.setDeliveryVolume(deliveryVolume);
        return bookedProductsDto;
    }

    @Transactional
    public boolean addProduct(AddProductToWarehouseRequest addProductToWarehouseRequest) {
        log.info("Добавление товара: {} на склад", addProductToWarehouseRequest);

        UUID uuid;
        try {
            uuid = UUID.fromString(addProductToWarehouseRequest.getProductId());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID " + addProductToWarehouseRequest.getProductId());
        }
        if (!productRepository.existsByProductId(uuid)) {
            throw new NoSpecifiedProductInWarehouseException("Добавляем товар не обнаружен на складе", HttpStatus.BAD_REQUEST, "Товар с UUID " + addProductToWarehouseRequest.getProductId() + " не обнаружен на складе");
        }

        ProductInWarehouse product = productRepository.findByProductId(uuid).get();
        product.setQuantity(product.getQuantity() + addProductToWarehouseRequest.getQuantity());
        ProductInWarehouse savedProduct = productRepository.save(product);
        log.info("Количество товара изменено на: {}", addProductToWarehouseRequest.getQuantity());

        return true;
    }

    public AddressDto getWarehouseAddress() {
        return WAREHOUSE_DATA.get(CURRENT_ADDRESS);
    }
}
