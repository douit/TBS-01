package sa.tamkeentech.tbs.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import sa.tamkeentech.tbs.domain.Report;
import sa.tamkeentech.tbs.service.dto.ReportDTO;

@Mapper(componentModel = "spring")
public interface ReportMapper extends EntityMapper<ReportDTO, Report> {

    @Mapping(source = "requestUser.id", target = "requestUserId")
    ReportDTO toDto(Report report);

    DataTablesOutput<ReportDTO> toDto(DataTablesOutput<Report> reportDataTablesOutput);
}
