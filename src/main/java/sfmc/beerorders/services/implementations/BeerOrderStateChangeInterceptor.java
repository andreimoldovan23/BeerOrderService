package sfmc.beerorders.services.implementations;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sfmc.beerorders.domain.BeerOrder;
import sfmc.beerorders.domain.BeerOrderEvent;
import sfmc.beerorders.domain.BeerOrderStatus;
import sfmc.beerorders.repositories.BeerOrderRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatus, BeerOrderEvent> {
    private final BeerOrderRepository beerOrderRepository;

    @Transactional
    @Override
    public void preStateChange(State<BeerOrderStatus, BeerOrderEvent> state, Message<BeerOrderEvent> message, Transition<BeerOrderStatus, BeerOrderEvent> transition, StateMachine<BeerOrderStatus, BeerOrderEvent> stateMachine) {
        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable((String) msg.getHeaders()
                        .getOrDefault(BeerOrderManagerServiceImpl.ORDER_ID_HEADER, " ")))
                .ifPresent(id -> {
            log.trace("saving state for order {}: {}", id, state.getId());

            BeerOrder beerOrder = beerOrderRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new RuntimeException("No such order"));
            beerOrder.setOrderStatus(state.getId());
            beerOrderRepository.saveAndFlush(beerOrder);
        });
    }
}
