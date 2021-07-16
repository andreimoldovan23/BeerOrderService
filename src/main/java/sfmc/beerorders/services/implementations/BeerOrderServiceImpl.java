package sfmc.beerorders.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.domain.Customer;
import sfmc.beerorders.domain.BeerOrderStatus;
import sfmc.beerorders.repositories.BeerOrderRepository;
import sfmc.beerorders.repositories.CustomerRepository;
import sfmc.beerorders.services.interfaces.BeerOrderService;
import sfmc.beerorders.web.mappers.BeerOrderMapper;
import sfmc.beerorders.web.model.BeerOrderDTO;
import sfmc.beerorders.web.model.BeerOrderPagedList;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerOrderServiceImpl implements BeerOrderService {
    private final BeerOrderRepository beerOrderRepository;
    private final CustomerRepository customerRepository;
    private final BeerOrderMapper beerOrderMapper;
//    private final ApplicationEventPublisher publisher;

    @Transactional
    @Override
    public BeerOrderPagedList listOrders(UUID customerId, Pageable pageable) {
        log.trace("Listing orders w/ customerId {}", customerId);

        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (customerOptional.isPresent()) {
            Page<BeerOrder> beerOrderPage =
                    beerOrderRepository.findAllByCustomer(customerOptional.get(), pageable);

            return new BeerOrderPagedList(beerOrderPage.stream()
                    .map(beerOrderMapper::beerOrderToDto)
                    .collect(Collectors.toList()), PageRequest.of(
                        beerOrderPage.getPageable().getPageNumber(),
                        beerOrderPage.getPageable().getPageSize()),
                        beerOrderPage.getTotalElements()
                    );
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public BeerOrderDTO placeOrder(UUID customerId, BeerOrderDTO beerOrderDto) {
        log.trace("Placing order from customer {}, with value {}", customerId, beerOrderDto);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                        log.trace("Customer w/ id {} not found", customerId);
                        return new RuntimeException("Customer not found");
                });

        BeerOrder beerOrder = beerOrderMapper.dtoToBeerOrder(beerOrderDto);
        beerOrder.setId(null);
        beerOrder.setCustomer(customer);
        beerOrder.setOrderStatus(BeerOrderStatus.NEW);

        beerOrder.getBeerOrderLines().forEach(line -> line.setBeerOrder(beerOrder));

        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);

        customer.getBeerOrders().add(savedBeerOrder);
        customerRepository.save(customer);

        log.trace("Saved Beer Order: {}", beerOrder.getId());

            //todo impl
            //  publisher.publishEvent(new NewBeerOrderEvent(savedBeerOrder));

        return beerOrderMapper.beerOrderToDto(savedBeerOrder);
    }

    @Transactional
    @Override
    public BeerOrderDTO getOrderById(UUID customerId, UUID orderId) {
        log.trace("Getting order w/ customerId {}, orderId {}", customerId, orderId);
        return beerOrderMapper.beerOrderToDto(getOrder(customerId, orderId));
    }

    @Transactional
    @Override
    public void pickupOrder(UUID customerId, UUID orderId) {
        log.trace("Picking up order w/ customerId {}, w/ id {}", customerId, orderId);

        BeerOrder beerOrder = getOrder(customerId, orderId);
        beerOrder.setOrderStatus(BeerOrderStatus.PICKED_UP);
        beerOrderRepository.save(beerOrder);
    }

    private BeerOrder getOrder(UUID customerId, UUID orderId) {
        log.trace("Searching order w/ customerId {}, w/ id {}", customerId, orderId);

        return customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.trace("Customer w/ id {} not found", customerId);
                    return new RuntimeException("Customer not found");
                })
                    .getBeerOrders().stream()
                        .filter(beerOrder -> beerOrder.getId().equals(orderId))
                        .findFirst()
                            .orElseThrow(() -> {
                                log.trace("Order w/ id {} not found", orderId);
                                return new RuntimeException("Beer Order not found");
                            });
    }
}
