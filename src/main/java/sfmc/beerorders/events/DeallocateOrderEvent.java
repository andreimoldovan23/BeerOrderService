package sfmc.beerorders.events;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sfmc.beerorders.web.model.BeerOrderDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeallocateOrderEvent implements Serializable {
    private BeerOrderDTO beerOrderDTO;
}