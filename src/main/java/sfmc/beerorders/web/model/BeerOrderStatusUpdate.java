package sfmc.beerorders.web.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BeerOrderStatusUpdate extends BaseItem {
    private UUID orderId;
    private String customerRef;
    private String orderStatus;
}
