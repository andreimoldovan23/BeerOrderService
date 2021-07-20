package sfmc.beerorders.services.testcomponents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import sfmc.beerorders.config.JmsConfig;
import sfmc.beerorders.events.AllocateOrderEvent;
import sfmc.beerorders.events.AllocationResponseEvent;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderAllocationListener {
    private final JmsTemplate template;
    @Value("${fail.allocation}") private String customerRefAllocationFail;
    @Value("${partial.allocation}") private String customerRefAllocationPartial;
    @Value("${cancel.allocation}") private String customerRefCancel;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message<?> msg) {
        AllocateOrderEvent orderEvent = (AllocateOrderEvent) msg.getPayload();

        log.trace("Got event: {}", orderEvent);

        if (orderEvent.getBeerOrderDTO().getCustomerRef() == null ||
            !orderEvent.getBeerOrderDTO().getCustomerRef().equals(customerRefCancel)) {
            Boolean error = orderEvent.getBeerOrderDTO().getCustomerRef() != null &&
                    orderEvent.getBeerOrderDTO().getCustomerRef().equals(customerRefAllocationFail);

            Boolean partial = orderEvent.getBeerOrderDTO().getCustomerRef() != null &&
                    orderEvent.getBeerOrderDTO().getCustomerRef().equals(customerRefAllocationPartial);

            Integer minusQuantity = partial ? 1 : 0;

            orderEvent.getBeerOrderDTO().getBeerOrderLines()
                    .forEach(line -> line.setQuantityAllocated(line.getOrderQuantity() - minusQuantity));

            template.convertAndSend(JmsConfig.ALLOCATION_RESPONSE_QUEUE,
                    new AllocationResponseEvent(orderEvent.getBeerOrderDTO(), error, partial));
        }
    }
}
