package sfmc.beerorders.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import sfmc.beerorders.domain.BeerOrderLine;
import sfmc.beerorders.web.model.BeerOrderLineDTO;

@Mapper(uses = {DateMapper.class})
@DecoratedWith(BeerOrderLineDecorator.class)
public interface BeerOrderLineMapper {
    BeerOrderLineDTO beerOrderLineToDto(BeerOrderLine line);

    BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDTO dto);
}
