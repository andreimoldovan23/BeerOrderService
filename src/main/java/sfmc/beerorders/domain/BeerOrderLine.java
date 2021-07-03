package sfmc.beerorders.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"beerOrder"})
@EqualsAndHashCode(callSuper = true, exclude = {"beerOrder"})

@Entity
public class BeerOrderLine extends BaseEntity {
    @ManyToOne
    private BeerOrder beerOrder;

    private UUID beerId;

    @Builder.Default
    private Integer orderQuantity = 0;

    @Builder.Default
    private Integer quantityAllocated = 0;
}
