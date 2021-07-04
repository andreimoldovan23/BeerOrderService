package sfmc.beerorders.web.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import sfmc.beerorders.domain.BeerOrderLine;
import sfmc.beerorders.services.beer.interfaces.BeerService;
import sfmc.beerorders.web.model.BeerOrderLineDTO;

public abstract class BeerOrderLineDecorator implements BeerOrderLineMapper {
    private BeerOrderLineMapper mapper;
    private BeerService beerService;

    @Autowired
    public void setMapper(BeerOrderLineMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Override
    public BeerOrderLineDTO beerOrderLineToDto(BeerOrderLine beerOrderLine) {
        BeerOrderLineDTO dto = mapper.beerOrderLineToDto(beerOrderLine);

        beerService.getBeerByUpc(dto.getUpc()).ifPresent(beerDTO -> {
            dto.setBeerStyle(beerDTO.getBeerType());
            dto.setPrice(beerDTO.getPrice());
            dto.setBeerName(beerDTO.getBeerName());
        });

        return dto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDTO dto) {
        return mapper.dtoToBeerOrderLine(dto);
    }
}
