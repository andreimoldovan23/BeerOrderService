package sfmc.beerorders.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sfmc.beerorders.services.BeerOrderService;
import sfmc.beerorders.web.model.BeerOrderDTO;
import sfmc.beerorders.web.model.BeerOrderPagedList;

import java.util.UUID;

@Slf4j
@RequestMapping("/api/v1/customers/{customerId}/")
@RestController
public class BeerOrderController {

    @Value("${default.page.number}") private Integer DEFAULT_PAGE_NUMBER;
    @Value("${default.page.size}") private Integer DEFAULT_PAGE_SIZE;

    private final BeerOrderService beerOrderService;

    public BeerOrderController(BeerOrderService beerOrderService) {
        this.beerOrderService = beerOrderService;
    }

    @GetMapping("orders")
    public BeerOrderPagedList listOrders(@PathVariable UUID customerId,
                                         @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                         @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        log.trace("Listing order w/ customerId {}", customerId);

        if (pageNumber == null || pageNumber < 0){
            pageNumber = DEFAULT_PAGE_NUMBER;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        log.trace("Page number {}, page size {}", pageNumber, pageSize);

        return beerOrderService.listOrders(customerId, PageRequest.of(pageNumber, pageSize));
    }

    @PostMapping("orders")
    @ResponseStatus(HttpStatus.CREATED)
    public BeerOrderDTO placeOrder(@PathVariable UUID customerId, @RequestBody BeerOrderDTO beerOrderDto) {
        log.trace("Placing order w/ customerId {}, value {}", customerId, beerOrderDto);
        return beerOrderService.placeOrder(customerId, beerOrderDto);
    }

    @GetMapping("orders/{orderId}")
    public BeerOrderDTO getOrder(@PathVariable UUID customerId, @PathVariable UUID orderId) {
        log.trace("Getting order w/ customerId {}, orderId {}", customerId, orderId);
        return beerOrderService.getOrderById(customerId, orderId);
    }

    @PutMapping("/orders/{orderId}/pickup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pickupOrder(@PathVariable UUID customerId, @PathVariable UUID orderId) {
        log.trace("Picking up order w/ customerId {}, orderId {}", customerId, orderId);
        beerOrderService.pickupOrder(customerId, orderId);
    }
}
