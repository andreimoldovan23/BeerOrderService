package sfmc.beerorders.web.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sfmc.beerorders.services.interfaces.CustomerService;
import sfmc.beerorders.web.model.CustomerPagedList;

@Slf4j
@RequestMapping("/api/v1/customers/")
@RestController
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @Value("${default.page.number}") private Integer DEFAULT_PAGE_NUMBER;
    @Value("${default.page.size}") private Integer DEFAULT_PAGE_SIZE;

    @GetMapping
    public CustomerPagedList listOrders(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                         @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        log.trace("Listing customers");

        if (pageNumber == null || pageNumber < 0){
            pageNumber = DEFAULT_PAGE_NUMBER;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        log.trace("Page number {}, page size {}", pageNumber, pageSize);

        return customerService.getCustomers(PageRequest.of(pageNumber, pageSize));
    }
}
