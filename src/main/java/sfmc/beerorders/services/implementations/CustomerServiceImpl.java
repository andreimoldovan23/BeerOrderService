package sfmc.beerorders.services.implementations;

import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sfmc.beerorders.domain.Customer;
import sfmc.beerorders.repositories.CustomerRepository;
import sfmc.beerorders.services.interfaces.CustomerService;
import sfmc.beerorders.web.mappers.CustomerMapper;
import sfmc.beerorders.web.model.CustomerPagedList;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerPagedList getCustomers(Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findAll(pageable);

        return new CustomerPagedList(
                customerPage.stream()
                    .map(customerMapper::customerToDTO)
                    .collect(Collectors.toList()),
                PageRequest.of(customerPage.getPageable().getPageNumber(),
                        customerPage.getPageable().getPageSize()),
                customerPage.getTotalElements()
        );
    }
}
