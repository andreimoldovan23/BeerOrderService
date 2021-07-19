package sfmc.beerorders.services.testcomponents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message<?> msg) {
        AllocateOrderEvent orderEvent = (AllocateOrderEvent) msg.getPayload();

        log.trace("Got event: {}", orderEvent);

        orderEvent.getBeerOrderDTO().getBeerOrderLines()
                .forEach(line -> line.setQuantityAllocated(line.getOrderQuantity()));

        template.convertAndSend(JmsConfig.ALLOCATION_RESPONSE_QUEUE,
                new AllocationResponseEvent(orderEvent.getBeerOrderDTO(), false, false));
    }
}
