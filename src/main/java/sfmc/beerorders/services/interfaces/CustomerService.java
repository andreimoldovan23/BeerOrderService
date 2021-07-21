package sfmc.beerorders.services.interfaces;

import org.springframework.data.domain.Pageable;
import sfmc.beerorders.web.model.CustomerPagedList;

public interface CustomerService {
    CustomerPagedList getCustomers(Pageable pageable);
}
