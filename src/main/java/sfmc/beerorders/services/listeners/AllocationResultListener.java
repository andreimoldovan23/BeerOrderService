package sfmc.beerorders.services.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import sfmc.beerorders.config.JmsConfig;
import sfmc.beerorders.events.AllocationResponseEvent;
import sfmc.beerorders.services.interfaces.BeerOrderManagerService;

@Component
@Slf4j
@RequiredArgsConstructor
public class AllocationResultListener {
    private final BeerOrderManagerService beerOrderManagerService;

    @JmsListener(destination = JmsConfig.ALLOCATION_RESPONSE_QUEUE)
    public void listen(AllocationResponseEvent event) {
        log.trace("Error: {}, Pending: {}, Order: {}", event.getAllocationError(), event.getPendingInventory(),
                event.getBeerOrderDTO());

        beerOrderManagerService.processAllocationResult(event.getBeerOrderDTO(), event.getAllocationError(),
                event.getPendingInventory());
    }
}
