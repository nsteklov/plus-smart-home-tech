package ru.yandex.practicum.commerce.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.commerce.exception.ProductInShoppingCartLowQuantityInWarehouse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {

    // используем стандартный декодер для всех кодов, которые не обработаем явно
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {

        if (methodKey.contains("checkProductsInWarehouse") && response.status() == 400) {
            return new ProductInShoppingCartLowQuantityInWarehouse("Не хватает товаров на складе", HttpStatus.CONTINUE, extractResponseBody(response));
        }

        return defaultDecoder.decode(methodKey, response);
    }

    private String extractResponseBody(Response response) {
        try {
            if (response.body() != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body().asInputStream()))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            }
        } catch (Exception e) {
            log.error("Failed to read response body", e);
        }
        return "";
    }
}
