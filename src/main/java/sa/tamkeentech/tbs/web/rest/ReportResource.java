package sa.tamkeentech.tbs.web.rest;

import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.domain.enumeration.ReportType;
import sa.tamkeentech.tbs.service.ReportService;
import sa.tamkeentech.tbs.service.dto.FileDTO;
import sa.tamkeentech.tbs.service.dto.PaymentMethodDTO;
import sa.tamkeentech.tbs.service.dto.ReportDTO;
import sa.tamkeentech.tbs.service.dto.ReportRequestDTO;

import javax.validation.Valid;
import java.util.Optional;

/**
 * Created by Ahmed B on 16/01/2020.
 */
@RestController
@RequestMapping("/api/report")
public class ReportResource {

    private final Logger log = LoggerFactory.getLogger(ReportResource.class);

    @Autowired
    private ReportService reportService;

    @PostMapping("/payment")
    // @PreAuthorize("hasAuthority('"+ PermissionConsts.Authorities.CREATE_REPORT +"')")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportDTO> createReport(@Valid @RequestBody ReportRequestDTO reportRequest){
        return ResponseEntity.ok(reportService.requestReport(reportRequest, ReportType.PAYMENT));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/payment/datatable")
    public DataTablesOutput<ReportDTO> getAllPaymentReports(DataTablesInput input) {
        return reportService.getReports(input, ReportType.PAYMENT);
    }

    @PostMapping("/refund")
    // @PreAuthorize("hasAuthority('"+ PermissionConsts.Authorities.CREATE_REPORT +"')")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportDTO> createRefundReport(@Valid @RequestBody ReportRequestDTO reportRequest){
        return ResponseEntity.ok(reportService.requestReport(reportRequest, ReportType.REFUND));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/refund/datatable")
    public DataTablesOutput<ReportDTO> getAllRefundReports(DataTablesInput input) {
        return reportService.getReports(input, ReportType.REFUND);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDTO> getPaymentMethod(@PathVariable Long id) {
        log.debug("REST request to get report : {}", id);
        Optional<FileDTO> reportDTO = reportService.getPaymentReport(id);
        return ResponseUtil.wrapOrNotFound(reportDTO);
    }

}
