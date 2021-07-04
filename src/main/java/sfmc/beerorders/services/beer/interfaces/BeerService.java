package sfmc.beerorders.services.beer.interfaces;

import sfmc.beerorders.services.beer.model.BeerDTO;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {
    Optional<BeerDTO> getBeerById(UUID id);
    Optional<BeerDTO> getBeerByUpc(String upc);
}
