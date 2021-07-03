package sfmc.beerorders.web.mappers;

import org.mapstruct.Mapper;
import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.web.model.BeerOrderDTO;

@Mapper(uses = {DateMapper.class, BeerOrderLineMapper.class})
public interface BeerOrderMapper {
    BeerOrderDTO beerOrderToDto(BeerOrder beerOrder);

    BeerOrder dtoToBeerOrder(BeerOrderDTO dto);
}
