package sfmc.beerorders.services.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import sfmc.beerorders.config.JmsConfig;
import sfmc.beerorders.events.ValidationResponseEvent;
import sfmc.beerorders.services.interfaces.BeerOrderManagerService;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidationResultListener {
    private final JmsTemplate jmsTemplate;
    private final BeerOrderManagerService beerOrderManagerService;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(ValidationResponseEvent event) {
        log.trace("Got result {} for order {}", event.getIsValid(), event.getOrderId());

        beerOrderManagerService.processValidationResult(event.getOrderId(), event.getIsValid());
    }
}
