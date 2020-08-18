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
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.domain.enumeration.ReportStatus;
import sa.tamkeentech.tbs.domain.enumeration.ReportType;
import sa.tamkeentech.tbs.domain.enumeration.RequestStatus;
import sa.tamkeentech.tbs.repository.ReportRepository;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.service.mapper.ReportMapper;
import sa.tamkeentech.tbs.service.util.CommonUtils;
import sa.tamkeentech.tbs.service.util.JasperReportExporter;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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
    private static final String FORMAT_PDF = "pdf";
    private static final String REPORT_KEY_FORMAT = "format";
    private static final String TEMPLATE_PAYMENT = "payment_report.jrxml";
    private static final String TEMPLATE_INVOICE = "ar_invoice_receipt.jrxml";
    private static final String TEMPLATE_REFUND = "refund_report.jrxml";
    private static final String ALL_FILTER = "All";
    private static final String PARAM_GENERATED_DATE = "generatedDate";
    private static final String PAYMENT_FILE_SUFFIX = "payment_report_";
    private static final String INVOICE_FILE_SUFFIX = "invoice_receipt_";
    private static final String PAYMENT_FOLDER_NAME = "payments";
    private static final String INVOICE_FOLDER_NAME = "invoice";
    private static final String REFUND_FILE_SUFFIX = "refund_report_";
    private static final String REFUND_FOLDER_NAME = "refunds";

    private static final String CUSTOMER_TRAINEE =   "اسم المتدرب  ";
    private static final String CUSTOMER_INSTITUTE = "اسم المنشأة  ";
    private static final String CUSTOMER_GENERAL =   "اسم العميل  ";


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
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private RefundService refundService;
    @Autowired
    private ItemService itemService;

    @Value("${tbs.report.reports-folder}")
    private String outputFolder;
    @Value("${tbs.report.resource-directory}")
    private String reportResourceDirectory;
    @Value("${tbs.payment.vat-number}")
    private String vatNumber;


    public ReportDTO requestReport(ReportRequestDTO reportRequest, ReportType reportType) {
        Report reportEntity = new Report();
        reportEntity.setRequestDate(ZonedDateTime.now());
        reportEntity.setStatus(ReportStatus.WAITING);
        reportEntity.setRequestUser(userService.getUser().get());
        reportEntity.setType(reportType);
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
        generateReport(reportEntity.getId(), clientName, reportType);
        return reportMapper.toDto(reportEntity);
    }

    @Async
    public void generateReport(Long reportId, String clientName, ReportType reportType) {
        log.debug("generating payment report id ---> {}", reportId);
        Report reportEntity = reportRepository.getOne(reportId);
        reportEntity.setStatus(ReportStatus.IN_PROGRESS);
        reportRepository.save(reportEntity);
        Long clientId = (reportEntity.getClient() != null) ? reportEntity.getClient().getId() : null;

        List<?> dataList;
        Map<String, Object> extraParams;
        String dirPath;
        String reportFileName;
        String template;
        switch (reportType) {
            case REFUND:
                dataList = refundService.getRefundsBetween(reportEntity.getStartDate(), reportEntity.getEndDate(), clientId);
                extraParams = refundReportExtraParams(reportEntity, (List<RefundDetailedDTO>) dataList, clientName);
                dirPath = outputFolder + "/" + REFUND_FOLDER_NAME + "/";
                reportFileName = REFUND_FILE_SUFFIX + reportId + ".xlsx";
                template = TEMPLATE_REFUND;
                break;
            default:
                dataList = paymentService.getPaymentsBetween(reportEntity.getStartDate(), reportEntity.getEndDate(), clientId);
                extraParams = paymentReportExtraParams(reportEntity, (List<PaymentDTO>) dataList, clientName);
                dirPath = outputFolder + "/" + PAYMENT_FOLDER_NAME + "/";
                reportFileName = PAYMENT_FILE_SUFFIX + reportId + ".xlsx";
                template = TEMPLATE_PAYMENT;
                break;

        }

        log.debug("report size ---> {}", dataList.size());
        try {
            generateReport(template, dirPath, reportFileName, dataList, extraParams, FORMAT_XLSX);
            reportEntity.setStatus(ReportStatus.READY);
            reportEntity.setGeneratedDate(ZonedDateTime.now());
            reportEntity.setExpireDate(getExpireDate(reportEntity.getType()));
            reportRepository.save(reportEntity);
        } catch (IOException| JRException e) {
            log.warn("Unable to generate report {}", reportId, e);
            reportEntity.setStatus(ReportStatus.FAILED);
            reportRepository.save(reportEntity);
        }
    }

    public FileDTO generateInvoiceReceipt( Long invoiceId){
        log.debug("generating invoice receipt ");

        Optional<InvoiceDTO> invoiceDTO = invoiceService.findOne(invoiceId);
        invoiceDTO.get().setVatNumber(vatNumber);

        ReportDTO receipt = new ReportDTO();
        receipt.setGeneratedDate(ZonedDateTime.now());
        receipt.setType(ReportType.INVOICE_RECEIPT);
        receipt.setClientName(invoiceDTO.get().getClient().getName());
        receipt.setClientId(invoiceDTO.get().getClient().getId());

        List<?> dataList;
        Map<String, Object> extraParams = new HashMap<>();
        String dirPath;
        String reportFileName;
        String template;

        dataList = invoiceDTO.get().getInvoiceItems();

        extraParams = invoiceReportExtraParams(receipt, invoiceDTO.get());
        dirPath = outputFolder + "/" + INVOICE_FOLDER_NAME + "/";
        reportFileName = INVOICE_FILE_SUFFIX + invoiceId + ".pdf";
        template = TEMPLATE_INVOICE;

        byte[] report = null;
        try {
            report = generateReport(template, dirPath, reportFileName, dataList, extraParams, FORMAT_PDF);
        } catch (IOException| JRException e) {
            log.warn("Unable to generate receipt {}", e);

        }

        return FileDTO.builder().name(reportFileName).bytes(report).build();
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
                    .filter(x -> x != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        extraParams.put("numberOfPayments", dataList.size());
        extraParams.put("totalPaymentsAmount", totalPaymentsAmount);
        return extraParams;
    }

    private Map<String, Object> refundReportExtraParams(Report report, List<RefundDetailedDTO> dataList, String clientName) {

        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("client", clientName);
        extraParams.put("startDate", CommonUtils.getFormattedLocalDate(report.getStartDate(), report.getOffset()));
        extraParams.put("endDate", CommonUtils.getFormattedLocalDate(report.getEndDate(), report.getOffset()));
        // report summary
        BigDecimal totalPaymentsAmount = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(dataList) ) {
            totalPaymentsAmount = dataList.stream()
                .filter(x -> (x.getStatus() == RequestStatus.SUCCEEDED))
                .map(RefundDetailedDTO::getAmount)
                .filter(x -> x != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        extraParams.put("numberOfRefunds", dataList.size());
        extraParams.put("totalRefundsAmount", totalPaymentsAmount);
        return extraParams;
    }
    private Map<String, Object> invoiceReportExtraParams(ReportDTO receipt, InvoiceDTO invoiceDTO) {
        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("invoice", invoiceDTO);
        extraParams.put("reportResource", reportResourceDirectory);
        // extraParams.put("generatedDate", CommonUtils.getFormattedLocalDate(receipt.getGeneratedDate(), Constants.RIYADH_OFFSET));

        Optional<Item> item = itemService.findByCodeAndClient(invoiceDTO.getInvoiceItems().get(0).getItemCode(), invoiceDTO.getClient().getId());
        if (item.get().getCategory().getCode().equals("COURSES")) {
            extraParams.put("customerLabel", CUSTOMER_TRAINEE);
        } else if (item.get().getCategory().getCode().equals("ADVERTISING")) {
            extraParams.put("customerLabel", CUSTOMER_INSTITUTE);
        } else {
            extraParams.put("customerLabel", CUSTOMER_GENERAL);
        }

        return extraParams;
    }


    private byte[] generateReport(String templateFile, String dirPath, String reportFileName, List<?> dataList
        , Map<String, Object> extraParams, String format) throws IOException, JRException {

        Map<String, Object> parameterMap = new HashMap<>();
        if (extraParams != null) {
            parameterMap.putAll(extraParams);
        }
        parameterMap.put(REPORT_KEY_FORMAT, format);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        parameterMap.put(PARAM_GENERATED_DATE, formatter.format(new Date()));

        byte[] report;
        if (format.equals(FORMAT_XLSX)) {
            report = JasperReportExporter.getInstance().generateXlsReport(dataList, parameterMap, templateFile);
        } else {
            report = JasperReportExporter.getInstance().generatePdfReport(dataList, parameterMap, templateFile, true);
        }
        fileWrapper.saveBytesToFile(dirPath, reportFileName, report);
        return report;
    }

    public DataTablesOutput<ReportDTO> getReports(DataTablesInput input, ReportType reportType) {
        return reportMapper.toDto(reportRepository.findAll(input, (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("type"), reportType)));
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("requestUser").get("id"), userService.getUser().get().getId())));
            predicates.add(criteriaBuilder.and(criteriaBuilder.or((criteriaBuilder.greaterThanOrEqualTo(root.get("expireDate"), ZonedDateTime.now())), criteriaBuilder.isNull(root.get("expireDate")))));
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));

        }));
    }

    private ZonedDateTime getExpireDate(ReportType type) {
        switch (type) {
            case PAYMENT:
                return ZonedDateTime.now().plusDays(7);
            case REFUND:
                return ZonedDateTime.now().plusDays(7);
            default:
                return null;
        }
    }

    public Optional<FileDTO> getReport(Long reportId) {
        Report report = reportRepository.getOne(reportId);
        User user = userService.getUser().get();
        if (report == null) {
            return Optional.empty();
        }
        if (!report.getRequestUser().getId().equals(user.getId())) {
            throw new TbsRunTimeException("User is not authorized to access report.");
        }
        String fileName;
        String filePath;

        switch (report.getType()) {
            case REFUND:
                fileName = REFUND_FILE_SUFFIX + reportId + ".xlsx";
                filePath = outputFolder + "/" + REFUND_FOLDER_NAME + "/" + fileName;
                break;
            default:
                fileName = PAYMENT_FILE_SUFFIX + reportId + ".xlsx";
                filePath = outputFolder + "/" + PAYMENT_FOLDER_NAME + "/" + fileName;
                break;
        }

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
