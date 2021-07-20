package sfmc.beerorders.services.testcomponents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${fail.validation}") private String customerRefFail;
    @Value("${cancel.validation}") private String customerRefCancel;

    private final JmsTemplate template;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(Message<?> msg) {
        ValidateOrderEvent orderEvent = (ValidateOrderEvent) msg.getPayload();

        log.trace("Got event: {}", orderEvent);

        if (orderEvent.getBeerOrderDTO().getCustomerRef() == null ||
            !orderEvent.getBeerOrderDTO().getCustomerRef().equals(customerRefCancel)) {
            template.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                    new ValidationResponseEvent(orderEvent.getBeerOrderDTO().getId(),
                            orderEvent.getBeerOrderDTO().getCustomerRef() == null ||
                                    !orderEvent.getBeerOrderDTO().getCustomerRef().equals(customerRefFail)));
        }
    }
}
