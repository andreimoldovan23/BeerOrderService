package sfmc.beerorders.config.actions;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import sfmc.beerorders.config.JmsConfig;
import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.domain.BeerOrderEvent;
import sfmc.beerorders.domain.BeerOrderStatus;
import sfmc.beerorders.events.ValidateOrderEvent;
import sfmc.beerorders.repositories.BeerOrderRepository;
import sfmc.beerorders.services.implementations.BeerOrderManagerServiceImpl;
import sfmc.beerorders.web.mappers.BeerOrderMapper;
import sfmc.beerorders.web.model.BeerOrderDTO;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatus, BeerOrderEvent> {
    private final JmsTemplate jmsTemplate;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        UUID id = UUID.fromString((String) stateContext.getMessage().getHeaders()
                .getOrDefault(BeerOrderManagerServiceImpl.ORDER_ID_HEADER, " "));

        log.trace("Got id from message: {}", id);
        BeerOrder beerOrder = beerOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("No such order"));

        log.trace("Got order from db: {}", beerOrder);
        BeerOrderDTO dto = beerOrderMapper.beerOrderToDto(beerOrder);

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE, new ValidateOrderEvent(dto));
    }
}
