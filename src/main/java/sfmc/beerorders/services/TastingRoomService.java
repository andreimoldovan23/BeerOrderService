package sfmc.beerorders.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sfmc.beerorders.bootstrap.DefaultBootstrap;
import sfmc.beerorders.domain.Customer;
import sfmc.beerorders.repositories.CustomerRepository;
import sfmc.beerorders.web.model.BeerOrderDTO;
import sfmc.beerorders.web.model.BeerOrderLineDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class TastingRoomService {
    @Value("${max.quantity}") private Integer MAX_QUANTITY;

    private final CustomerRepository customerRepository;
    private final BeerOrderService beerOrderService;
    private final List<String> beerUpcs = new ArrayList<>(3);

    public TastingRoomService(CustomerRepository customerRepository, BeerOrderService beerOrderService) {
        this.customerRepository = customerRepository;
        this.beerOrderService = beerOrderService;

        beerUpcs.add(DefaultBootstrap.BEER_1_UPC);
        beerUpcs.add(DefaultBootstrap.BEER_2_UPC);
        beerUpcs.add(DefaultBootstrap.BEER_3_UPC);
    }

    @Transactional
    @Scheduled(fixedRate = 2000, initialDelay = 2000)
    public void placeTastingRoomOrder(){
        log.trace("Placing tasting room order...");
        List<Customer> customerList = customerRepository.findAllByCustomerNameLike(DefaultBootstrap.TASTING_ROOM);

        if (customerList.size() == 1) {
            doPlaceOrder(customerList.get(0));
        } else {
            log.trace("Too many or too few tasting room customers found");
        }
    }

    private void doPlaceOrder(Customer customer) {
        log.trace("Placing order for {}", customer);

        String beerToOrder = getRandomBeerUpc();
        log.trace("UPC {}", beerToOrder);

        BeerOrderLineDTO beerOrderLine = BeerOrderLineDTO.builder()
                .upc(beerToOrder)
                .orderQuantity(new Random().nextInt(MAX_QUANTITY))
                .build();
        log.trace("Beer order line {}", beerOrderLine);

        BeerOrderDTO beerOrder = BeerOrderDTO.builder()
                .customerId(customer.getId())
                .customerRef(UUID.randomUUID().toString())
                .beerOrderLine(beerOrderLine)
                .build();
        log.trace("Beer to order {}", beerOrder);

        BeerOrderDTO savedOrder = beerOrderService.placeOrder(customer.getId(), beerOrder);
        log.trace("Saved order {}", savedOrder);
    }

    private String getRandomBeerUpc() {
        return beerUpcs.get(new Random().nextInt(beerUpcs.size()));
    }
}
