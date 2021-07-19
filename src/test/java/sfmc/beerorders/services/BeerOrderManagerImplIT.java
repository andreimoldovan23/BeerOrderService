package sfmc.beerorders.services;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.domain.BeerOrderLine;
import sfmc.beerorders.domain.BeerOrderStatus;
import sfmc.beerorders.domain.Customer;
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
public class BeerOrderManagerImplIT {

    private static final String beerApiUpc = "/api/v1/beerUpc/12345";

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

        BeerOrder savedOrder = managerService.newOrder(order);

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

        assertNotNull(savedOrder);
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
