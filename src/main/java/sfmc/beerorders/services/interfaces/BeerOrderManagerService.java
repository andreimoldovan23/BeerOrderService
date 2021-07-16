package sfmc.beerorders.services.interfaces;

import sfmc.beerorders.domain.BeerOrder;

public interface BeerOrderManagerService {
    BeerOrder newOrder(BeerOrder beerOrder);
}
