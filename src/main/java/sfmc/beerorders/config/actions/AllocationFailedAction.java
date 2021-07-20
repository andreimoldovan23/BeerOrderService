package sfmc.beerorders.config.actions;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import sfmc.beerorders.config.JmsConfig;
import sfmc.beerorders.domain.BeerOrderEvent;
import sfmc.beerorders.domain.BeerOrderStatus;
import sfmc.beerorders.events.AllocationFailedEvent;
import sfmc.beerorders.services.implementations.BeerOrderManagerServiceImpl;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationFailedAction implements Action<BeerOrderStatus, BeerOrderEvent> {
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        UUID id = UUID.fromString((String) stateContext.getMessage().getHeaders()
                .getOrDefault(BeerOrderManagerServiceImpl.ORDER_ID_HEADER, " "));

        log.trace("Allocation failed for: {}", id);

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATION_FAILED_QUEUE, new AllocationFailedEvent(id));
    }
}