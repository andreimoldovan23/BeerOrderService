package sfmc.beerorders.web.mappers;

import org.mapstruct.Mapper;
import sfmc.beerorders.domain.Customer;
import sfmc.beerorders.web.model.CustomerDTO;

@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {
    Customer dtoToCustomer(CustomerDTO dto);
    CustomerDTO customerToDTO(Customer customer);
}
