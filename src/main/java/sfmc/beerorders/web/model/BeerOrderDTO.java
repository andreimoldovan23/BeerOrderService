package sfmc.beerorders.web.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import sfmc.beerorders.domain.OrderStatusEnum;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BeerOrderDTO extends BaseItem {
    private UUID customerId;
    private String customerRef;

    @Singular
    private List<BeerOrderLineDTO> beerOrderLines;

    private OrderStatusEnum orderStatus;
    private String orderStatusCallbackUrl;
}
