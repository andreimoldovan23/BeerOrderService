package sfmc.beerorders.config;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import sfmc.beerorders.events.AllocateOrderEvent;
import sfmc.beerorders.events.AllocationResponseEvent;
import sfmc.beerorders.events.ValidateOrderEvent;
import sfmc.beerorders.events.ValidationResponseEvent;
import sfmc.beerorders.web.model.BeerOrderDTO;
import sfmc.beerorders.web.model.BeerOrderLineDTO;

@Configuration
public class JmsConfig {
    public static final String VALIDATE_ORDER_QUEUE = "validate-order-request";
    public static final String VALIDATE_ORDER_RESPONSE_QUEUE = "validate-order-response";
    public static final String ALLOCATE_ORDER_QUEUE = "allocate-order-request";
    public static final String ALLOCATION_RESPONSE_QUEUE = "allocate-request-response";

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);

        HashMap<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put(ValidateOrderEvent.class.getSimpleName(), ValidateOrderEvent.class);
        typeIdMappings.put(BeerOrderDTO.class.getSimpleName(), BeerOrderDTO.class);
        typeIdMappings.put(BeerOrderLineDTO.class.getSimpleName(), BeerOrderLineDTO.class);
        typeIdMappings.put(ValidationResponseEvent.class.getSimpleName(), ValidationResponseEvent.class);
        typeIdMappings.put(AllocateOrderEvent.class.getSimpleName(), AllocateOrderEvent.class);
        typeIdMappings.put(AllocationResponseEvent.class.getSimpleName(), AllocationResponseEvent.class);

        converter.setTypeIdMappings(typeIdMappings);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
