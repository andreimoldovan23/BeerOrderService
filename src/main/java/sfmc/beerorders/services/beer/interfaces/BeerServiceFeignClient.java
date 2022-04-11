package sfmc.beerorders.services.beer.interfaces;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sfmc.beerorders.services.beer.model.BeerDTO;

@FeignClient(name = "beer-service")
public interface BeerServiceFeignClient {
    @GetMapping("/api/v1/beer/{beerId}")
    BeerDTO getById(@PathVariable UUID beerId);

    @GetMapping("/api/v1/beerUpc/{beerUpc}")
    BeerDTO getByUpc(@PathVariable String beerUpc);
}
