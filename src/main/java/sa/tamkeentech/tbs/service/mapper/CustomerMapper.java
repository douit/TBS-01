package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.CustomerDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Customer} and its DTO {@link CustomerDTO}.
 */
@Mapper(componentModel = "spring", uses = {ContactMapper.class})
public interface CustomerMapper extends EntityMapper<CustomerDTO, Customer> {

    @Mapping(source = "contact.id", target = "contactId")
    CustomerDTO toDto(Customer customer);

    @Mapping(source = "contactId", target = "contact")
    Customer toEntity(CustomerDTO customerDTO);

    default Customer fromId(Long id) {
        if (id == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }
}
