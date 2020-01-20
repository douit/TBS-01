package sa.tamkeentech.tbs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.domain.Client;
import sa.tamkeentech.tbs.domain.Report;
import sa.tamkeentech.tbs.domain.User;
import sa.tamkeentech.tbs.domain.enumeration.ReportStatus;
import sa.tamkeentech.tbs.domain.enumeration.ReportType;
import sa.tamkeentech.tbs.repository.ReportRepository;
import sa.tamkeentech.tbs.service.dto.FileDTO;
import sa.tamkeentech.tbs.service.dto.PaymentDTO;
import sa.tamkeentech.tbs.service.dto.ReportDTO;
import sa.tamkeentech.tbs.service.dto.ReportRequestDTO;
import sa.tamkeentech.tbs.service.mapper.ReportMapper;
import sa.tamkeentech.tbs.service.util.CommonUtils;
import sa.tamkeentech.tbs.service.util.JasperReportExporter;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Created by Ahmed B.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReportService {

    private static final String FORMAT_XLSX = "xlsx";
    private static final String REPORT_KEY_FORMAT = "format";
    private static final String TEMPLATE_PAYMENT = "payment_report.jrxml";
    private static final String ALL_FILTER = "All";
    private static final String PARAM_GENERATED_DATE = "generatedDate";
    private static final String FILE_SUFFIX = "payment_report_";
    private static final String PAYMENT_FOLDER_NAME = "payments";

    @Autowired
    private UserService userService;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ClientService clientService;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private FileWrapper fileWrapper;
    @Autowired
    private PaymentService paymentService;

    @Value("${tbs.report.reports-folder}")
    private String outputFolder;

    public ReportDTO requestPaymentReport(ReportRequestDTO reportRequest) {
        Report reportEntity = new Report();
        reportEntity.setRequestDate(ZonedDateTime.now());
        reportEntity.setStatus(ReportStatus.WAITING);
        reportEntity.setRequestUser(userService.getUser().get());
        reportEntity.setType(ReportType.PAYMENT);
        String clientName = ALL_FILTER;
        if (reportRequest.getClientId() != null) {
            // Client
            Optional<Client> client = clientService.getClientById(reportRequest.getClientId());
            reportEntity.setClient(client.get());
            clientName = client.get().getName();
        }

        reportEntity.setStartDate(reportRequest.getStartDate());
        reportEntity.setEndDate(reportRequest.getEndDate());
        reportEntity.setOffset(reportRequest.getOffset());

        reportEntity = reportRepository.save(reportEntity);
        generatePaymentReport(reportEntity.getId(), clientName);
        return reportMapper.toDto(reportEntity);
    }


    public Page<ReportDTO> listReports(Long userId, Pageable pageable) {
        Page<Report> reportList = reportRepository.findByRequestUserId(userId, pageable);
        return reportList.map(reportMapper::toDto);
    }

    public ReportDTO getReport(Long userId, Long reportId) {
        Report reportEntity = reportRepository.getOne(reportId);
        if (!reportEntity.getRequestUser().getId().equals(userId)) {
            throw new TbsRunTimeException("User is not authorized to access report.");
        }
        return reportMapper.toDto(reportEntity);
    }

    @Async
    public void generatePaymentReport(Long reportId, String clientName) {
        log.debug("generating payment report id ---> {}", reportId);
        Report reportEntity = reportRepository.getOne(reportId);
        reportEntity.setStatus(ReportStatus.IN_PROGRESS);
        reportRepository.save(reportEntity);
        Long clientId = (reportEntity.getClient() != null) ? reportEntity.getClient().getId() : null;

        List<PaymentDTO> dataList = paymentService.getPaymentsBetween(reportEntity.getStartDate(), reportEntity.getEndDate(), clientId);
        log.debug("report size ---> {}", dataList.size());
        Map<String, Object> extraParams = paymentReportExtraParams(reportEntity, dataList, clientName);
        String reportFileName = FILE_SUFFIX + reportId /*+ "_" + System.currentTimeMillis()*/ + ".xlsx";
        try {
            String reportUrl = generateReport(TEMPLATE_PAYMENT, reportFileName, dataList, extraParams);
            reportEntity.setStatus(ReportStatus.READY);
            reportEntity.setGeneratedDate(ZonedDateTime.now());
            // reportEntity.setDownloadUrl(reportUrl);
            reportEntity.setExpireDate(getExpireDate(reportEntity.getType()));
            reportRepository.save(reportEntity);
        } catch (IOException| JRException e) {
            log.warn("Unable to generate report {}", reportId, e);
            reportEntity.setStatus(ReportStatus.FAILED);
            reportRepository.save(reportEntity);
        }
    }

    private Map<String, Object> paymentReportExtraParams(Report report, List<PaymentDTO> dataList, String clientName) {

        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("client", clientName);
        extraParams.put("startDate", CommonUtils.getFormattedLocalDate(report.getStartDate(), report.getOffset()));
        extraParams.put("endDate", CommonUtils.getFormattedLocalDate(report.getEndDate(), report.getOffset()));
        // report summary
        BigDecimal totalPaymentsAmount = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(dataList) ) {
            totalPaymentsAmount = dataList.stream().map(PaymentDTO::getAmount)
                    .filter(x -> x != null).reduce(BigDecimal::add).get();
        }
        extraParams.put("numberOfPayments", dataList.size());
        extraParams.put("totalPaymentsAmount", totalPaymentsAmount);
        return extraParams;
    }

    private String generateReport(String templateFile, String reportFileName, List<?> dataList, Map<String, Object> extraParams) throws IOException, JRException {

        Map<String, Object> parameterMap = new HashMap<>();
        if (extraParams != null) {
            parameterMap.putAll(extraParams);
        }
        parameterMap.put(REPORT_KEY_FORMAT, FORMAT_XLSX);
        parameterMap.put(PARAM_GENERATED_DATE, new Date());

        byte[] report = JasperReportExporter.getInstance().generateXlsReport(dataList, parameterMap, templateFile);
        String dirPath = outputFolder + "/" + PAYMENT_FOLDER_NAME + "/";
        return fileWrapper.saveBytesToFile(dirPath, reportFileName, report);
    }

    public DataTablesOutput<ReportDTO> getPaymentReports(DataTablesInput input) {
        return reportMapper.toDto(reportRepository.findAll(input, (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("requestUser").get("id"), userService.getUser().get().getId())));
            predicates.add(criteriaBuilder.and(criteriaBuilder.or((criteriaBuilder.greaterThanOrEqualTo(root.get("expireDate"), ZonedDateTime.now())), criteriaBuilder.isNull(root.get("expireDate")))));
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }));
    }

    private ZonedDateTime getExpireDate(ReportType type) {
        switch (type) {
            case PAYMENT:
                return ZonedDateTime.now().plusDays(1);
            case REFUND:
                return ZonedDateTime.now().plusDays(1);
            default:
                return null;
        }
    }

    public Optional<FileDTO> getPaymentReport(Long reportId) {
        Report report = reportRepository.getOne(reportId);
        User user = userService.getUser().get();
        if (report == null || !user.getId().equals(report.getRequestUser().getId())) {
            return Optional.empty();
        }
        String fileName = FILE_SUFFIX + reportId /*+ "_" + System.currentTimeMillis()*/ + ".xlsx";
        String filePath = outputFolder + "/" + PAYMENT_FOLDER_NAME + "/" + fileName;
        byte[] file;
        try {
            file = fileWrapper.extractBytes(filePath);
        } catch (IOException e) {
            return Optional.empty();
        }
        FileDTO fileDTO = FileDTO.builder()
            .id(reportId)
            .name(fileName)
            .bytes(file)
            .build();
        return Optional.of(fileDTO);
    }
}
