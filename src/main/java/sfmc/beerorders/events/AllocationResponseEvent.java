package sfmc.beerorders.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sfmc.beerorders.web.model.BeerOrderDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationResponseEvent {
    private BeerOrderDTO beerOrderDTO;
    private Boolean allocationError;
    private Boolean pendingInventory;
}
