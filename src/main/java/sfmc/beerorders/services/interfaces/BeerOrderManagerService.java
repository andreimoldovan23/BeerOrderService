package sfmc.beerorders.services.interfaces;

import java.util.UUID;

import sfmc.beerorders.domain.BeerOrder;

public interface BeerOrderManagerService {
    BeerOrder newOrder(BeerOrder beerOrder);
    void processValidationResult(UUID orderId, Boolean isValid);
}
