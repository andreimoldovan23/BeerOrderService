package sfmc.beerorders.services.interfaces;

import java.util.UUID;

import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.web.model.BeerOrderDTO;

public interface BeerOrderManagerService {
    BeerOrder newOrder(BeerOrder beerOrder);
    void processValidationResult(UUID orderId, Boolean isValid);
    void processAllocationResult(BeerOrderDTO dto, Boolean allocationError, Boolean pendingInventory);
}
