package sfmc.beerorders.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"beerOrders", "apiKey"})
@EqualsAndHashCode(callSuper = true, exclude = {"beerOrders"})

@Entity
public class Customer extends BaseEntity {
    private String customerName;

    @Type(type="org.hibernate.type.UUIDCharType")
    @Column(length = 36, columnDefinition = "varchar(36)")
    private UUID apiKey;

    @Singular
    @OneToMany(mappedBy = "customer")
    @Fetch(FetchMode.JOIN)
    private Set<BeerOrder> beerOrders = new HashSet<>();
}
