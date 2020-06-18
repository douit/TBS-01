package sa.tamkeentech.tbs.service.mapper;

import org.springframework.data.domain.Page;

public interface BasePagingMapper<D, E> extends EntityMapper<D, E> {
    default Page<D> toDtoPageable(Page<E> page) {
        return page.map(this::toDto);
    }
}
