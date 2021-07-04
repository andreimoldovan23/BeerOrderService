package sfmc.beerorders.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.web.model.BeerOrderDTO;

@Mapper(uses = {DateMapper.class, BeerOrderLineMapper.class})
public interface BeerOrderMapper {
    @Mapping(target = "customerId", source = "customer.id")
    BeerOrderDTO beerOrderToDto(BeerOrder beerOrder);

    BeerOrder dtoToBeerOrder(BeerOrderDTO dto);
}
