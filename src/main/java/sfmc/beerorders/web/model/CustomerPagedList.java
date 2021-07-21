package sfmc.beerorders.web.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class CustomerPagedList extends PageImpl<CustomerDTO> implements Serializable {
    public CustomerPagedList(List<CustomerDTO> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public CustomerPagedList(List<CustomerDTO> content) {
        super(content);
    }
}
