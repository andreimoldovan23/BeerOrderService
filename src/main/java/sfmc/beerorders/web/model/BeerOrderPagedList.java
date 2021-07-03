package sfmc.beerorders.web.model;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class BeerOrderPagedList extends PageImpl<BeerOrderDTO> {
    public BeerOrderPagedList(List<BeerOrderDTO> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public BeerOrderPagedList(List<BeerOrderDTO> content) {
        super(content);
    }
}
