package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.dto.ShoppingCartDto;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/shopping-cart")
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PutMapping
    public ShoppingCartDto addProduct(
            @RequestBody Map<String, Integer> products,
            @RequestParam String username) {
        log.info("POST запрос на добавление товаров в корзину: {}", products);
        return shoppingCartService.addProducts(username, products);
    }

    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam String username) {

        log.info("GET запрос на получение актуальной корзины по авторизованному пользователю : {}", username);
        return shoppingCartService.getShoppingCart(username);
    }

    @DeleteMapping
    public boolean deleteShoppingCart(@RequestParam String username) {

        log.info("DELETE запрос на удаление корзины по авторизованному пользователю : {}", username);
        return shoppingCartService.deleteShoppingCart(username);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeFromShoppingCart(@RequestBody String[] products, @RequestParam String username) {

        log.info("POST запрос на удаление товаров из корзины по авторизованному пользователю : {}", username);
        return shoppingCartService.removeFromShoppingCart(products, username);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeQuantity(
            @RequestBody ChangeProductQuantityRequest changeProductQuantityRequest,
            @RequestParam String username) {

        log.info("POST запрос на изменение количества товаров из корзины по авторизованному пользователю : {}", username);
        return shoppingCartService.changeQuantity(changeProductQuantityRequest, username);
    }

    @GetMapping("/username")
    public String getUserNameByProductCartUUID(@RequestParam String uuid) {

        log.info("GET запрос на получение имемни пользтвателя корзины покупателя: {}", uuid);
        return shoppingCartService.getUserNameByProductCartUUID(uuid);
    }
}
