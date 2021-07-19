package sfmc.beerorders.services.implementations;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.domain.BeerOrderEvent;
import sfmc.beerorders.domain.BeerOrderStatus;
import sfmc.beerorders.repositories.BeerOrderRepository;
import sfmc.beerorders.services.interfaces.BeerOrderManagerService;
import sfmc.beerorders.web.model.BeerOrderDTO;

@Service
@RequiredArgsConstructor
public class BeerOrderManagerServiceImpl implements BeerOrderManagerService {
    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final BeerOrderRepository beerOrderRepository;
    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory;
    private final BeerOrderStateChangeInterceptor interceptor;

    @Transactional
    @Override
    public BeerOrder newOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatus.NEW);
        beerOrder = beerOrderRepository.save(beerOrder);
        sendEvent(beerOrder, BeerOrderEvent.VALIDATION);
        return beerOrder;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID orderId, Boolean isValid) {
        BeerOrder beerOrder = beerOrderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("No such order"));
        if (isValid) {
            sendEvent(beerOrder, BeerOrderEvent.VALIDATION_PASSED);

            beerOrder = beerOrderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("No such order"));
            sendEvent(beerOrder, BeerOrderEvent.ALLOCATION);
        } else {
            sendEvent(beerOrder, BeerOrderEvent.VALIDATION_FAILED);
        }
    }

    @Transactional
    @Override
    public void processAllocationResult(BeerOrderDTO dto, Boolean allocationError, Boolean pendingInventory) {
        BeerOrder beerOrder = beerOrderRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("No such order"));
        if (!allocationError && !pendingInventory) {
            sendEvent(beerOrder, BeerOrderEvent.ALLOCATION_SUCCESS);
        } else if (!allocationError) {
            sendEvent(beerOrder, BeerOrderEvent.ALLOCATION_NO_INVENTORY);
        } else {
            sendEvent(beerOrder, BeerOrderEvent.ALLOCATION_FAILED);
            return;
        }
        updateQuantity(dto);
    }

    @Transactional
    @Override
    public void pickOrderUp(UUID id) {
        BeerOrder beerOrder = beerOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("No such order"));
        sendEvent(beerOrder, BeerOrderEvent.BEER_ORDER_PICKED_UP);
    }

    private void updateQuantity(BeerOrderDTO dto) {
        BeerOrder beerOrder = beerOrderRepository.getById(dto.getId());

        beerOrder.getBeerOrderLines().forEach(orderLine -> dto.getBeerOrderLines().forEach(line -> {
            if (line.getId().equals(orderLine.getId())) {
                orderLine.setQuantityAllocated(line.getQuantityAllocated());
            }
        }));

        beerOrderRepository.saveAndFlush(beerOrder);
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
