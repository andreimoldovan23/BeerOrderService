package sfmc.beerorders.services.implementations;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.domain.BeerOrderEvent;
import sfmc.beerorders.domain.BeerOrderStatus;
import sfmc.beerorders.repositories.BeerOrderRepository;
import sfmc.beerorders.services.interfaces.BeerOrderManagerService;
import sfmc.beerorders.web.model.BeerOrderDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderManagerServiceImpl implements BeerOrderManagerService {
    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final BeerOrderRepository beerOrderRepository;
    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory;
    private final BeerOrderStateChangeInterceptor interceptor;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public BeerOrder newOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatus.NEW);
        beerOrder = beerOrderRepository.saveAndFlush(beerOrder);
        sendEvent(beerOrder, BeerOrderEvent.VALIDATION);
        return beerOrder;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public void processValidationResult(UUID orderId, Boolean isValid) {
        beerOrderRepository.findById(orderId).ifPresentOrElse(beerOrder -> {
            if (isValid) {
                sendEvent(beerOrder, BeerOrderEvent.VALIDATION_PASSED);
//                awaitStatusChange(orderId, BeerOrderStatus.VALIDATED);
                sendEvent(beerOrder, BeerOrderEvent.ALLOCATION);
            } else {
                sendEvent(beerOrder, BeerOrderEvent.VALIDATION_FAILED);
            }
        }, () -> log.trace("Validation - No such order: {}", orderId));

    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public void processAllocationResult(BeerOrderDTO dto, Boolean allocationError, Boolean pendingInventory) {
        beerOrderRepository.findById(dto.getId()).ifPresentOrElse(beerOrder -> {
            if (!allocationError && !pendingInventory) {
                sendEvent(beerOrder, BeerOrderEvent.ALLOCATION_SUCCESS);
//                awaitStatusChange(dto.getId(), BeerOrderStatus.ALLOCATED);
            } else if (!allocationError) {
                sendEvent(beerOrder, BeerOrderEvent.ALLOCATION_NO_INVENTORY);
//                awaitStatusChange(dto.getId(), BeerOrderStatus.PENDING_INVENTORY);
            } else {
                sendEvent(beerOrder, BeerOrderEvent.ALLOCATION_FAILED);
                return;
            }
            updateQuantity(dto);
        }, () -> log.trace("Allocation - No such order: {}", dto.getId()));
    }

    @Transactional
    @Override
    public void pickOrderUp(UUID id) {
        beerOrderRepository.findById(id).ifPresentOrElse(
                beerOrder -> sendEvent(beerOrder, BeerOrderEvent.BEER_ORDER_PICKED_UP),
                () -> log.trace("Pick up - No such order: {}", id)
        );
    }

    @Transactional
    @Override
    public void cancelOrder(UUID id) {
        beerOrderRepository.findById(id).ifPresentOrElse(
                beerOrder -> sendEvent(beerOrder, BeerOrderEvent.CANCEL_ORDER),
                () -> log.trace("Cancel - No such order: {}", id)
        );
    }

//    private void awaitStatusChange(UUID beerOrderId, BeerOrderStatus statusEnum) {
//        AtomicBoolean found = new AtomicBoolean(false);
//        AtomicInteger loopCount = new AtomicInteger(0);
//
//        while (!found.get()) {
//            if (loopCount.incrementAndGet() > 10) {
//                found.set(true);
//                log.trace("Loop Retries exceeded for {}", beerOrderId);
//                break;
//            }
//
//            beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
//                if (beerOrder.getOrderStatus().equals(statusEnum)) {
//                    found.set(true);
//                    log.trace("Order {} Found. Tries {}", beerOrderId, loopCount.get());
//                } else {
//                    log.trace("Order Status for {} Not Equal. Expected: {} Found {} Tries {}", beerOrderId, statusEnum.name(),
//                            beerOrder.getOrderStatus(), loopCount.get());
//                }
//            }, () -> log.trace("Order Id {} Not Found. Tries {}", beerOrderId, loopCount.get()));
//
//            if (!found.get()) {
//                try {
//                    log.trace("Sleeping for retry. Tries {}", loopCount.get());
//                    Thread.sleep(100);
//                } catch (Exception e) {
//                    // do nothing
//                }
//            }
//        }
//    }

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
