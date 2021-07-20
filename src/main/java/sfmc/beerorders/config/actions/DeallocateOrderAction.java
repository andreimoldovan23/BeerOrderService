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
import sfmc.beerorders.events.DeallocateOrderEvent;
import sfmc.beerorders.repositories.BeerOrderRepository;
import sfmc.beerorders.services.implementations.BeerOrderManagerServiceImpl;
import sfmc.beerorders.web.mappers.BeerOrderMapper;
import sfmc.beerorders.web.model.BeerOrderDTO;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeallocateOrderAction implements Action<BeerOrderStatus, BeerOrderEvent> {
    private final JmsTemplate jmsTemplate;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        UUID id = UUID.fromString((String) stateContext.getMessage().getHeaders()
                .getOrDefault(BeerOrderManagerServiceImpl.ORDER_ID_HEADER, " "));

        log.trace("Deallocating - Got id from message: {}", id);
        beerOrderRepository.findById(id).ifPresentOrElse(beerOrder -> {
            log.trace("Deallocating - Got order from db: {}", beerOrder);
            BeerOrderDTO dto = beerOrderMapper.beerOrderToDto(beerOrder);

            jmsTemplate.convertAndSend(JmsConfig.DEALLOCATE_ORDER_QUEUE, new DeallocateOrderEvent(dto));
        }, () -> log.trace("Deallocating action - no such order: {}", id));
    }
}
