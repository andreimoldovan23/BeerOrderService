package sfmc.beerorders.services.testcomponents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import sfmc.beerorders.config.JmsConfig;
import sfmc.beerorders.events.ValidateOrderEvent;
import sfmc.beerorders.events.ValidationResponseEvent;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderValidationListener {
    private final JmsTemplate template;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(Message<?> msg) {
        ValidateOrderEvent orderEvent = (ValidateOrderEvent) msg.getPayload();

        log.trace("Got event: {}", orderEvent);

        template.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                new ValidationResponseEvent(orderEvent.getBeerOrderDTO().getId(), true));
    }
}
