package sfmc.beerorders.services.interfaces;

import org.springframework.data.domain.Pageable;
import sfmc.beerorders.web.model.BeerOrderDTO;
import sfmc.beerorders.web.model.BeerOrderPagedList;

import java.util.UUID;

public interface BeerOrderService {
    BeerOrderPagedList listOrders(UUID customerId, Pageable pageable);

    BeerOrderDTO placeOrder(UUID customerId, BeerOrderDTO beerOrderDto);

    BeerOrderDTO getOrderById(UUID customerId, UUID orderId);

    void pickupOrder(UUID customerId, UUID orderId);
}
