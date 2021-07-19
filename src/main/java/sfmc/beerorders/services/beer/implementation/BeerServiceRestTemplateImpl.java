package sfmc.beerorders.services.beer.implementation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfmc.beerorders.services.beer.interfaces.BeerService;
import sfmc.beerorders.services.beer.model.BeerDTO;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class BeerServiceRestTemplateImpl implements BeerService {

    @Value("${beer.host}") private String beerHost;
    public static final String beerApiId = "/api/v1/beer/{beerId}";
    public static final String beerApiUpc = "/api/v1/beerUpc/{beerUpc}";

    private final RestTemplate restTemplate;

    public BeerServiceRestTemplateImpl(RestTemplateBuilder builder) {
        restTemplate = builder.build();
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID beerId) {
        log.trace("Getting beer by id - {}", beerId);
        return Optional.ofNullable(restTemplate.getForObject(beerHost + beerApiId, BeerDTO.class, beerId));
    }

    @Override
    public Optional<BeerDTO> getBeerByUpc(String beerUpc) {
        log.trace("Getting beer by upc - {}", beerUpc);
        return Optional.ofNullable(restTemplate.getForObject(beerHost + beerApiUpc, BeerDTO.class, beerUpc));
    }
}
