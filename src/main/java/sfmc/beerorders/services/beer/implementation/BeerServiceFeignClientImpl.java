package sfmc.beerorders.services.beer.implementation;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sfmc.beerorders.services.beer.interfaces.BeerService;
import sfmc.beerorders.services.beer.interfaces.BeerServiceFeignClient;
import sfmc.beerorders.services.beer.model.BeerDTO;

@Profile("localdiscovery")
@Slf4j
@RequiredArgsConstructor
@Service
public class BeerServiceFeignClientImpl implements BeerService {
    private final BeerServiceFeignClient feignClient;

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        log.trace("Getting beer by id - {}", id);
        return Optional.ofNullable(feignClient.getById(id));
    }

    @Override
    public Optional<BeerDTO> getBeerByUpc(String upc) {
        log.trace("Getting beer by upc - {}", upc);
        return Optional.ofNullable(feignClient.getByUpc(upc));
    }
}
