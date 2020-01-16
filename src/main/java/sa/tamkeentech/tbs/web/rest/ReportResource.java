package sa.tamkeentech.tbs.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.service.ReportService;
import sa.tamkeentech.tbs.service.dto.ReportDTO;
import sa.tamkeentech.tbs.service.dto.ReportRequestDTO;

import javax.validation.Valid;

/**
 * Created by Ahmed B on 16/01/2020.
 */
@RestController
@RequestMapping("/api/report")
public class ReportResource {

    @Autowired
    private ReportService reportService;

    @PostMapping("/payment")
    // @PreAuthorize("hasAuthority('"+ PermissionConsts.Authorities.CREATE_REPORT +"')")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportDTO> createTripReport(@Valid @RequestBody ReportRequestDTO reportRequest){
        return ResponseEntity.ok(reportService.requestPaymentReport(reportRequest));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/payment/datatable")
    public DataTablesOutput<ReportDTO> getAllPaymentAuditReports(DataTablesInput input) {
        return reportService.getPaymentReports(input);
    }

}
