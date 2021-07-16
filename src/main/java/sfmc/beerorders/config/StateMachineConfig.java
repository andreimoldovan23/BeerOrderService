package sfmc.beerorders.config;

import java.util.EnumSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
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
                .end(BeerOrderStatus.DELIVERY_EXCEPTION)
                .end(BeerOrderStatus.DELIVERY);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStatus, BeerOrderEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(BeerOrderStatus.NEW).target(BeerOrderStatus.NEW).event(BeerOrderEvent.VALIDATION)

                .and()

                .withExternal()
                .source(BeerOrderStatus.NEW).target(BeerOrderStatus.VALIDATED).event(BeerOrderEvent.VALIDATION_PASSED)

                .and()

                .withExternal()
                .source(BeerOrderStatus.NEW).target(BeerOrderStatus.VALIDATION_EXCEPTION).event(BeerOrderEvent.VALIDATION_FAILED)

                .and()

                .withExternal()
                .source(BeerOrderStatus.VALIDATED).target(BeerOrderStatus.VALIDATED).event(BeerOrderEvent.ALLOCATION)

                .and()

                .withExternal()
                .source(BeerOrderStatus.VALIDATED).target(BeerOrderStatus.ALLOCATED).event(BeerOrderEvent.ALLOCATION_SUCCESS)

                .and()

                .withExternal()
                .source(BeerOrderStatus.VALIDATED).target(BeerOrderStatus.ALLOCATION_EXCEPTION).event(BeerOrderEvent.ALLOCATION_FAILED)

                .and()

                .withExternal()
                .source(BeerOrderStatus.VALIDATED).target(BeerOrderStatus.PENDING_INVENTORY).event(BeerOrderEvent.ALLOCATION_NO_INVENTORY);
    }
}
