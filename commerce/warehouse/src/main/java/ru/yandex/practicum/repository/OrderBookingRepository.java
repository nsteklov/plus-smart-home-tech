package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.OrderBooking;

import java.util.Optional;
import java.util.UUID;

public interface OrderBookingRepository extends JpaRepository<OrderBooking, UUID> {

    @Query("SELECT ob FROM OrderBooking ob " +
            "LEFT JOIN FETCH ob.products p " +
            "WHERE ob.orderId = :orderId")
    Optional<OrderBooking> findByOrderId(@Param("orderId") UUID orderId);
}
