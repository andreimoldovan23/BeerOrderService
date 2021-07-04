package sfmc.beerorders.web.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BeerOrderLineDTO extends BaseItem {
    private String upc;
    private String beerName;
    private UUID beerId;
    private String beerStyle;
    private BigDecimal price;

    @Builder.Default
    private Integer orderQuantity = 0;
}
