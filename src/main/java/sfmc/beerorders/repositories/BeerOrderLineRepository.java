package sfmc.beerorders.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sfmc.beerorders.domain.BeerOrderLine;

import java.util.UUID;

public interface BeerOrderLineRepository extends JpaRepository<BeerOrderLine, UUID> {
}
