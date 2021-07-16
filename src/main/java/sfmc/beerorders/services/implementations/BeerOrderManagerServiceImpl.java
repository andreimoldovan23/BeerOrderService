package sfmc.beerorders.services.implementations;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.domain.BeerOrderEvent;
import sfmc.beerorders.domain.BeerOrderStatus;
import sfmc.beerorders.repositories.BeerOrderRepository;
import sfmc.beerorders.services.interfaces.BeerOrderManagerService;

@Service
@RequiredArgsConstructor
public class BeerOrderManagerServiceImpl implements BeerOrderManagerService {
    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final BeerOrderRepository beerOrderRepository;
    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory;
    private final BeerOrderStateChangeInterceptor interceptor;

    @Override
    public BeerOrder newOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatus.NEW);
        beerOrder = beerOrderRepository.save(beerOrder);
        sendEvent(beerOrder, BeerOrderEvent.VALIDATION);
        return beerOrder;
    }

    @Override
    public void processValidationResult(UUID orderId, Boolean isValid) {
        BeerOrder beerOrder = beerOrderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("No such order"));
        if (isValid) {
            sendEvent(beerOrder, BeerOrderEvent.VALIDATION_PASSED);
        } else {
            sendEvent(beerOrder, BeerOrderEvent.VALIDATION_FAILED);
        }
    }

    private void sendEvent(BeerOrder beerOrder, BeerOrderEvent event) {
        StateMachine<BeerOrderStatus, BeerOrderEvent> sm = resetStateMachine(beerOrder);

        Message<BeerOrderEvent> message = MessageBuilder.withPayload(event)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString()).build();

        sm.sendEvent(message);
    }

    private StateMachine<BeerOrderStatus, BeerOrderEvent> resetStateMachine(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatus, BeerOrderEvent> sm = stateMachineFactory.getStateMachine(beerOrder.getId().toString());

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(interceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
                });

        sm.start();

        return sm;
    }
}
