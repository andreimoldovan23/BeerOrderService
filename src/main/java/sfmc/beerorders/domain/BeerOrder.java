package sfmc.beerorders.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"beerOrderLines"})
@EqualsAndHashCode(callSuper = true, exclude = {"customer", "beerOrderLines"})

@Entity
public class BeerOrder extends BaseEntity {
    private String customerRef;

    @ManyToOne
    private Customer customer;

    @Singular
    @OneToMany(mappedBy = "beerOrder", cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private Set<BeerOrderLine> beerOrderLines = new HashSet<>();

    @Builder.Default
    private BeerOrderStatus orderStatus = BeerOrderStatus.NEW;

    private String orderStatusCallbackUrl;
}
