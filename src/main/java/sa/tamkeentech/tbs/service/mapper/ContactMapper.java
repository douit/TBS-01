package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.ContactDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Contact} and its DTO {@link ContactDTO}.
 */
@Mapper(componentModel = "spring", uses = {CountryMapper.class})
public interface ContactMapper extends EntityMapper<ContactDTO, Contact> {

    @Mapping(source = "country.id", target = "countryId")
    ContactDTO toDto(Contact contact);

    @Mapping(source = "countryId", target = "country")
    Contact toEntity(ContactDTO contactDTO);

    default Contact fromId(Long id) {
        if (id == null) {
            return null;
        }
        Contact contact = new Contact();
        contact.setId(id);
        return contact;
    }
}
