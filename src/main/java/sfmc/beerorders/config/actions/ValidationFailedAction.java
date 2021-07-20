package sfmc.beerorders.config.actions;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import sfmc.beerorders.domain.BeerOrderEvent;
import sfmc.beerorders.domain.BeerOrderStatus;
import sfmc.beerorders.services.implementations.BeerOrderManagerServiceImpl;

@Slf4j
@Component
public class ValidationFailedAction implements Action<BeerOrderStatus, BeerOrderEvent> {
    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        UUID id = UUID.fromString((String) stateContext.getMessage().getHeaders()
                .getOrDefault(BeerOrderManagerServiceImpl.ORDER_ID_HEADER, " "));
        log.trace("Validation failed for: {}", id);
    }
}
