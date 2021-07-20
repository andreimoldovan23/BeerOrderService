package sfmc.beerorders.config;

import java.util.EnumSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import sfmc.beerorders.domain.BeerOrderEvent;
import sfmc.beerorders.domain.BeerOrderStatus;

@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
@Slf4j
public class StateMachineConfig extends StateMachineConfigurerAdapter<BeerOrderStatus, BeerOrderEvent> {
    private final Action<BeerOrderStatus, BeerOrderEvent> validateOrderAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> allocateOrderAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> validationFailedAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> allocationFailedAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> deallocateOrderAction;

    @Override
    public void configure(StateMachineConfigurationConfigurer<BeerOrderStatus, BeerOrderEvent> config) throws Exception {
        StateMachineListenerAdapter<BeerOrderStatus, BeerOrderEvent> listener = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<BeerOrderStatus, BeerOrderEvent> from, State<BeerOrderStatus, BeerOrderEvent> to) {
                log.trace("state changed from {} to {}", from.getId(), to.getId());
            }
        };

        config.withConfiguration().listener(listener);
    }

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStatus, BeerOrderEvent> states) throws Exception {
        states.withStates()
                .initial(BeerOrderStatus.NEW)
                .states(EnumSet.allOf(BeerOrderStatus.class))
                .end(BeerOrderStatus.VALIDATION_EXCEPTION)
                .end(BeerOrderStatus.ALLOCATION_EXCEPTION)
                .end(BeerOrderStatus.PICKED_UP)
                .end(BeerOrderStatus.CANCELED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStatus, BeerOrderEvent> transitions) throws Exception {
        transitions
                //new -> pending validation
                .withExternal()
                .source(BeerOrderStatus.NEW).target(BeerOrderStatus.PENDING_VALIDATION)
                .event(BeerOrderEvent.VALIDATION)
                .action(validateOrderAction)
                .and()

                //pending validation -> validated
                .withExternal()
                .source(BeerOrderStatus.PENDING_VALIDATION).target(BeerOrderStatus.VALIDATED)
                .event(BeerOrderEvent.VALIDATION_PASSED)
                .and()

                //pending validation -> validation exception
                .withExternal()
                .source(BeerOrderStatus.PENDING_VALIDATION).target(BeerOrderStatus.VALIDATION_EXCEPTION)
                .event(BeerOrderEvent.VALIDATION_FAILED)
                .action(validationFailedAction)
                .and()

                //pending validation -> canceled
                .withExternal()
                .source(BeerOrderStatus.PENDING_VALIDATION).target(BeerOrderStatus.CANCELED)
                .event(BeerOrderEvent.CANCEL_ORDER)
                .and()

                //validated -> pending allocation
                .withExternal()
                .source(BeerOrderStatus.VALIDATED).target(BeerOrderStatus.PENDING_ALLOCATION)
                .event(BeerOrderEvent.ALLOCATION)
                .action(allocateOrderAction)
                .and()

                //validated -> canceled
                .withExternal()
                .source(BeerOrderStatus.VALIDATED).target(BeerOrderStatus.CANCELED)
                .event(BeerOrderEvent.CANCEL_ORDER)
                .and()

                //pending allocation -> allocated
                .withExternal()
                .source(BeerOrderStatus.PENDING_ALLOCATION).target(BeerOrderStatus.ALLOCATED)
                .event(BeerOrderEvent.ALLOCATION_SUCCESS)
                .and()

                //pending allocation -> allocation exception
                .withExternal()
                .source(BeerOrderStatus.PENDING_ALLOCATION).target(BeerOrderStatus.ALLOCATION_EXCEPTION)
                .event(BeerOrderEvent.ALLOCATION_FAILED)
                .action(allocationFailedAction)
                .and()

                //pending allocation -> canceled
                .withExternal()
                .source(BeerOrderStatus.PENDING_ALLOCATION).target(BeerOrderStatus.CANCELED)
                .event(BeerOrderEvent.CANCEL_ORDER)
                .and()

                //pending allocation -> pending inventory
                .withExternal()
                .source(BeerOrderStatus.PENDING_ALLOCATION).target(BeerOrderStatus.PENDING_INVENTORY)
                .event(BeerOrderEvent.ALLOCATION_NO_INVENTORY)
                .and()

                //allocated -> picked up
                .withExternal()
                .source(BeerOrderStatus.ALLOCATED).target(BeerOrderStatus.PICKED_UP)
                .event(BeerOrderEvent.BEER_ORDER_PICKED_UP)
                .and()

                //allocated -> canceled
                .withExternal()
                .source(BeerOrderStatus.ALLOCATED).target(BeerOrderStatus.CANCELED)
                .event(BeerOrderEvent.CANCEL_ORDER)
                .action(deallocateOrderAction);
    }
}
