package sfmc.beerorders.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sfmc.beerorders.domain.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findAllByCustomerNameLike(String customerName);
}
