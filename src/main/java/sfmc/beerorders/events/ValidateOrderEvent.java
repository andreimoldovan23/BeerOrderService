package sfmc.beerorders.events;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import sfmc.beerorders.web.model.BeerOrderDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ValidateOrderEvent implements Serializable {
    private BeerOrderDTO beerOrderDTO;
}
