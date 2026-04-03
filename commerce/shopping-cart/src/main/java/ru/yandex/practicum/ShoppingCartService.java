package ru.yandex.practicum;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.feign.WarehouseClient;
import ru.yandex.practicum.commerce.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final WarehouseClient warehouseClient;

    @Transactional
    public ShoppingCartDto addProducts(String username, Map<String, Integer> products) {
        log.info("Добавление товаров: {} в корзину", products);

        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Передано пустое имя пользователя", HttpStatus.UNAUTHORIZED, "Имя пользователя не должно быть пустым");
        }

        ShoppingCart shoppingCart;
        Optional<ShoppingCart> optionalShoppingCart = shoppingCartRepository.findByUsername(username);
        if (optionalShoppingCart.isPresent()) {
            shoppingCart = optionalShoppingCart.get();
        } else {
            shoppingCart = new ShoppingCart();
            shoppingCart.setUsername(username);
        }

        Map<UUID, Integer> savedProducts = new HashMap<>();
        for (Map.Entry<String, Integer> entry : products.entrySet()) {
            try {
                UUID uuid = UUID.fromString(entry.getKey());
                savedProducts.put(uuid, entry.getValue());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Передан некорректный формат UUID " + entry.getKey());
            }
        }

        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        shoppingCartDto.setProducts(savedProducts);

        try {
            BookedProductsDto bookedProductsDto = warehouseClient.checkProductsInWarehouse(shoppingCartDto);
        } catch (ProductInShoppingCartLowQuantityInWarehouse productInShoppingCartLowQuantityInWarehouse) {
            throw productInShoppingCartLowQuantityInWarehouse;
        }

        shoppingCart.setProducts(savedProducts);
        ShoppingCart savedShoppingCart = shoppingCartRepository.save(shoppingCart);
        log.info("Создана корзина покупателя с UUID: {}", savedShoppingCart.getShoppingCartId());

        shoppingCartDto.setShoppingCartId(savedShoppingCart.getShoppingCartId());
        return shoppingCartDto;
    }

    public ShoppingCartDto getShoppingCart(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Передано пустое имя пользователя", HttpStatus.UNAUTHORIZED, "Имя пользователя не должно быть пустым");
        }
        Optional<ShoppingCart> optionalShoppingCart = shoppingCartRepository.findByUsername(username);
        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        if (optionalShoppingCart.isPresent()) {
            ShoppingCart shoppingCart = optionalShoppingCart.get();
            shoppingCartDto.setShoppingCartId(shoppingCart.getShoppingCartId());
            shoppingCartDto.setProducts(shoppingCart.getProducts());
            return shoppingCartDto;
        }
        return null;
    }

    @Transactional
    public boolean deleteShoppingCart(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Передано пустое имя пользователя", HttpStatus.UNAUTHORIZED, "Имя пользователя не должно быть пустым");
        }
        Optional<ShoppingCart> optionalShoppingCart = shoppingCartRepository.findByUsername(username);
        if (optionalShoppingCart.isPresent()) {
            shoppingCartRepository.delete(optionalShoppingCart.get());
        }
        log.info("Удалена корзина по покупателю : {}", username);
        return true;
    }

    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String[] products, String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Передано пустое имя пользователя", HttpStatus.UNAUTHORIZED, "Имя пользователя не должно быть пустым");
        }
        Optional<ShoppingCart> optionalShoppingCart = shoppingCartRepository.findByUsername(username);
        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        if (optionalShoppingCart.isPresent()) {
            ShoppingCart shoppingCart = optionalShoppingCart.get();
            Map<UUID, Integer> productsFromShoppingCart = shoppingCart.getProducts();
            for (String stringUUID : products) {
                try {
                    UUID uuid = UUID.fromString(stringUUID);
                    if (productsFromShoppingCart.containsKey(uuid)) {
                        productsFromShoppingCart.remove(uuid);
                    } else {
                        throw new NoProductsInShoppingCartException("Отсутствуют товары в корзине", HttpStatus.BAD_REQUEST, "В корзине отсутствует товар с UUID: " + stringUUID);
                    }
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Передан некорректный формат UUID " + stringUUID);
                }
            }
            shoppingCartRepository.save(shoppingCart);
            shoppingCartDto.setShoppingCartId(shoppingCart.getShoppingCartId());
            shoppingCartDto.setProducts(shoppingCart.getProducts());
            log.info("Удалены товары из корзины по по покупателю : {}", username);
            return shoppingCartDto;
        }
        return null;
    }

    @Transactional
    public ShoppingCartDto changeQuantity(ChangeProductQuantityRequest changeProductQuantityRequest, String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Передано пустое имя пользователя", HttpStatus.UNAUTHORIZED, "Имя пользователя не должно быть пустым");
        }
        Optional<ShoppingCart> optionalShoppingCart = shoppingCartRepository.findByUsername(username);
        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        if (optionalShoppingCart.isPresent()) {
            ShoppingCart shoppingCart = optionalShoppingCart.get();
            Map<UUID, Integer> productsFromShoppingCart = shoppingCart.getProducts();
            try {
                UUID uuid = UUID.fromString(changeProductQuantityRequest.getProductId());
                if (productsFromShoppingCart.containsKey(uuid)) {
                    productsFromShoppingCart.put(uuid, changeProductQuantityRequest.getNewQuantity());
                } else {
                    throw new NoProductsInShoppingCartException("Отсутствуют товары в корзине", HttpStatus.BAD_REQUEST, "В корзине отсутствует товар с UUID: " + changeProductQuantityRequest.getProductId());
                }
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Передан некорректный формат UUID " + shoppingCart.getProducts());
            }

            shoppingCartDto.setShoppingCartId(shoppingCart.getShoppingCartId());
            shoppingCartDto.setProducts(shoppingCart.getProducts());

            try {
                BookedProductsDto bookedProductsDto = warehouseClient.checkProductsInWarehouse(shoppingCartDto);
            } catch (FeignException e) {
                throw new ProductInShoppingCartLowQuantityInWarehouse("Привет", HttpStatus.BAD_REQUEST, "sds");
            }

            shoppingCartRepository.save(shoppingCart);
            log.info("Изменено количество товаров в корзине по по покупателю : {}", username);
            return shoppingCartDto;
        }
        return null;
    }
}
