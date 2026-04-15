package ru.yandex.practicum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.Order;

import java.util.UUID;

public interface OrderRepository  extends JpaRepository<Order, UUID> {

    Page<Order> findByUsername(String Username, Pageable pageable);
}
