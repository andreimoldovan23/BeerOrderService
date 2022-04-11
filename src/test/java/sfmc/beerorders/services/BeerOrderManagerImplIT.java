package sfmc.beerorders.services;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import sfmc.beerorders.config.JmsConfig;
import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.domain.BeerOrderLine;
import sfmc.beerorders.domain.BeerOrderStatus;
import sfmc.beerorders.domain.Customer;
import sfmc.beerorders.events.AllocationFailedEvent;
import sfmc.beerorders.events.DeallocateOrderEvent;
import sfmc.beerorders.repositories.BeerOrderRepository;
import sfmc.beerorders.repositories.CustomerRepository;
import sfmc.beerorders.services.beer.model.BeerDTO;
import sfmc.beerorders.services.interfaces.BeerOrderManagerService;
import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(WireMockExtension.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("localdiscovery")
@Ignore
public class BeerOrderManagerImplIT {

    private static final String beerApiUpc = "/api/v1/beerUpc/12345";

    @Value("${fail.validation}") private String customerRefFail;
    @Value("${fail.allocation}") private String customerRefAllocationFail;
    @Value("${partial.allocation}") private String customerRefAllocationPartial;
    @Value("${cancel.validation}") private String customerRefValidationCancel;
    @Value("${cancel.allocation}") private String customerRefAllocationCancel;

    @Autowired
    BeerOrderManagerService managerService;

    @Autowired
    BeerOrderRepository orderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    WireMockServer server;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    JmsTemplate jmsTemplate;

    Customer testCustomer;

    UUID beerId = UUID.randomUUID();

    @TestConfiguration
    static class RestTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer server = with(wireMockConfig().port(8084));
            server.start();
            return server;
        }
    }

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder()
            .customerName("Test customer").build());
    }

    @Test
    public void testNewToPickedUp() throws JsonProcessingException {
        BeerOrder order = init(null);

        await().untilAsserted(() -> {
            BeerOrder savedOrder2 = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            assertEquals(BeerOrderStatus.ALLOCATED, savedOrder2.getOrderStatus());
            savedOrder2.getBeerOrderLines()
                    .forEach(line -> assertEquals(line.getQuantityAllocated(), line.getOrderQuantity()));
        });

        managerService.pickOrderUp(order.getId());

        await().untilAsserted(() -> {
            BeerOrder savedOrder2 = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            assertEquals(BeerOrderStatus.PICKED_UP, savedOrder2.getOrderStatus());
        });

        assertNotNull(order);
    }

    @Test
    public void testFailedValidation() throws JsonProcessingException {
        BeerOrder order = init(customerRefFail);

        await().untilAsserted(() -> {
            BeerOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            assertEquals(BeerOrderStatus.VALIDATION_EXCEPTION, savedOrder.getOrderStatus());
        });
    }

    @Test
    public void testFailedAllocation() throws JsonProcessingException {
        BeerOrder order = init(customerRefAllocationFail);

        await().untilAsserted(() -> {
            BeerOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            assertEquals(BeerOrderStatus.ALLOCATION_EXCEPTION, savedOrder.getOrderStatus());
        });

        AllocationFailedEvent event = (AllocationFailedEvent) jmsTemplate.receiveAndConvert(JmsConfig.ALLOCATION_FAILED_QUEUE);
        assertNotNull(event);
        assertEquals(event.getId(), order.getId());
    }

    @Test
    public void testPartialAllocation() throws JsonProcessingException {
        BeerOrder order = init(customerRefAllocationPartial);

        await().untilAsserted(() -> {
            BeerOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            savedOrder.getBeerOrderLines()
                    .forEach(line -> assertEquals(line.getQuantityAllocated(), line.getOrderQuantity() - 1));
            assertEquals(BeerOrderStatus.PENDING_INVENTORY, savedOrder.getOrderStatus());
        });
    }

    @Test
    public void testPendingValidationToCanceled() throws JsonProcessingException {
        BeerOrder order = init(customerRefValidationCancel);

        await().untilAsserted(() -> {
            BeerOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            assertEquals(BeerOrderStatus.PENDING_VALIDATION, savedOrder.getOrderStatus());
        });

        managerService.cancelOrder(order.getId());

        await().untilAsserted(() -> {
            BeerOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            assertEquals(BeerOrderStatus.CANCELED, savedOrder.getOrderStatus());
        });
    }

    @Test
    public void testPendingAllocationToCanceled() throws JsonProcessingException {
        BeerOrder order = init(customerRefAllocationCancel);

        await().untilAsserted(() -> {
            BeerOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            assertEquals(BeerOrderStatus.PENDING_ALLOCATION, savedOrder.getOrderStatus());
        });

        managerService.cancelOrder(order.getId());

        await().untilAsserted(() -> {
            BeerOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            assertEquals(BeerOrderStatus.CANCELED, savedOrder.getOrderStatus());
        });
    }

    @Test
    public void testAllocatedToCanceled() throws JsonProcessingException {
        BeerOrder order = init(null);

        await().untilAsserted(() -> {
            BeerOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            assertEquals(BeerOrderStatus.ALLOCATED, savedOrder.getOrderStatus());
        });

        managerService.cancelOrder(order.getId());

        await().untilAsserted(() -> {
            BeerOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new RuntimeException("No such order"));

            assertEquals(BeerOrderStatus.CANCELED, savedOrder.getOrderStatus());
        });

        DeallocateOrderEvent event = (DeallocateOrderEvent) jmsTemplate.receiveAndConvert(JmsConfig.DEALLOCATE_ORDER_QUEUE);
        assertNotNull(event);
        assertEquals(event.getBeerOrderDTO().getId(), order.getId());
    }

    private BeerOrder init(String ref) throws JsonProcessingException {
        BeerDTO beerDTO = BeerDTO.builder()
                .id(beerId)
                .upc("12345")
                .beerName("ALE")
                .beerType("ALE")
                .price(new BigDecimal("14.45"))
                .build();

        server.stubFor(get(beerApiUpc)
                .willReturn(okJson(mapper.writeValueAsString(beerDTO))));

        final BeerOrder order = createNewOrder();
        order.setCustomerRef(ref);

        return managerService.newOrder(order);
    }

    private BeerOrder createNewOrder() {
        BeerOrder order = BeerOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
            .beerId(beerId)
            .upc("12345")
            .orderQuantity(1)
            .beerOrder(order)
            .build());

        order.setBeerOrderLines(lines);
        return order;
    }

}
