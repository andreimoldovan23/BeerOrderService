package sfmc.beerorders.config;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import sfmc.beerorders.events.ValidateOrderEvent;
import sfmc.beerorders.events.ValidationResponseEvent;
import sfmc.beerorders.web.model.BeerOrderDTO;
import sfmc.beerorders.web.model.BeerOrderLineDTO;

@Configuration
public class JmsConfig {
    public static final String VALIDATE_ORDER_QUEUE = "validate-order-request";
    public static final String VALIDATE_ORDER_RESPONSE_QUEUE = "validate-order-response";

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);

        HashMap<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put(ValidateOrderEvent.class.getSimpleName(), ValidateOrderEvent.class);
        typeIdMappings.put(BeerOrderDTO.class.getSimpleName(), BeerOrderDTO.class);
        typeIdMappings.put(BeerOrderLineDTO.class.getSimpleName(), BeerOrderLineDTO.class);
        typeIdMappings.put(ValidationResponseEvent.class.getSimpleName(), ValidationResponseEvent.class);

        converter.setTypeIdMappings(typeIdMappings);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
