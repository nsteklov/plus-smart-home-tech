package ru.yandex.practicum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {

    @Query("SELECT sc FROM ShoppingCart sc " +
            "LEFT JOIN FETCH sc.products p " +
            "WHERE sc.username = :username")
    Optional<ShoppingCart> findByUsername(@Param("username") String username);
}
