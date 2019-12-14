package sa.tamkeentech.tbs.service;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.domain.enumeration.*;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.security.SecurityUtils;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.service.mapper.InvoiceMapper;
import sa.tamkeentech.tbs.service.mapper.ItemMapper;
import sa.tamkeentech.tbs.service.util.EventPublisherService;
import sa.tamkeentech.tbs.service.mapper.ItemMapper;
import sa.tamkeentech.tbs.service.util.SequenceUtil;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Service Implementation for managing {@link Invoice}.
 */
@Service
@Transactional
public class InvoiceService {

    private final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;

    private final InvoiceMapper invoiceMapper;

    private final ClientService clientService;

    private final CustomerService customerService;

    private final PaymentMethodService paymentMethodService;

    private final ItemService itemService;

    private final ItemMapper itemMapper;

    private final PaymentService paymentService;

    private final SequenceUtil sequenceUtil;

    private final EventPublisherService eventPublisherService;

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper, ClientService clientService, CustomerService customerService, PaymentMethodService paymentMethodService, ItemService itemService, PaymentService paymentService, SequenceUtil sequenceUtil, EventPublisherService eventPublisherService, ItemMapper itemMapper) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.clientService = clientService;
        this.customerService = customerService;
        this.paymentMethodService = paymentMethodService;
        this.itemService = itemService;
        this.paymentService = paymentService;
        this.sequenceUtil = sequenceUtil;
        this.eventPublisherService = eventPublisherService;
        this.itemMapper=itemMapper;
    }

    /**
     * Save a invoice.
     *
     * @param invoiceDTO the entity to save.
     * @return the persisted entity.
     */
    public InvoiceDTO save(InvoiceDTO invoiceDTO) {
        log.debug("Request to save Invoice : {}", invoiceDTO);
        Invoice invoice = invoiceMapper.toEntity(invoiceDTO);
        invoice = invoiceRepository.save(invoice);
        return invoiceMapper.toDto(invoice);
    }

    /**
     * Get all the invoices.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Invoices");
        return invoiceRepository.findAll(pageable)
            .map(invoiceMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceDTO> findByPaymentStatus(PaymentStatus status, Pageable pageable) {
        log.debug("Request to get all Invoices");
        return invoiceRepository.findByPaymentStatusOrderByIdDesc(status, pageable)
            .map(invoiceMapper::toDto);
    }


    /**
     * Get one invoice by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<InvoiceDTO> findOne(Long id) {
        log.debug("Request to get Invoice : {}", id);
        return invoiceRepository.findById(id)
            .map(invoiceMapper::toDto);
    }

    /**
     * Delete the invoice by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Invoice : {}", id);
        invoiceRepository.deleteById(id);
    }

    public InvoiceItemsResponseDTO saveInvoice(InvoiceDTO invoiceDTO) {

        Invoice invoice = creatNewInvoice(invoiceDTO);

        // Payment
        // Payment method
        Optional<PaymentMethod> paymentMethod = paymentMethodService.findById(invoiceDTO.getPaymentMethod().getId());

        InvoiceItemsResponseDTO invoiceItemsResponseDTO= InvoiceItemsResponseDTO.builder()
            .paymentMethod(invoiceDTO.getPaymentMethod().getId()).build();

        if(invoiceDTO.getAmount().equals(invoice.getAmount())){
            if (paymentMethod.isPresent()) {
                String paymentMethodCode = paymentMethod.get().getCode();
                switch (paymentMethodCode) {
                    case Constants.SADAD:
                        int sadadResult;
                        try {
                            sadadResult = paymentService.sadadCall(invoice.getNumber(), invoice.getAccountId().toString(), invoice.getAmount());
                        } catch (IOException | JSONException e) {
                            // ToDo add new exception 500 for sadad
                            throw new TbsRunTimeException("Sadad issue", e);
                        }
                        // ToDo add new exception 500 for sadad
                        // invoice = invoiceRepository.getOne(invoice.getId());
                        if (sadadResult != 200) {
                            invoiceItemsResponseDTO.setStatusId(sadadResult);
                            invoiceItemsResponseDTO.setShortDesc("error");
                            invoiceItemsResponseDTO.setDescription("error_message");
                            invoice.setStatus(InvoiceStatus.FAILED);
                            // invoiceRepository.save(invoice);
                            throw new TbsRunTimeException("Sadad bill creation error");
                        }
                        invoice.setStatus(InvoiceStatus.CREATED);
                        // ToDO save again issue
                        //invoiceRepository.save(invoice);
                        invoiceItemsResponseDTO.setStatusId(1);
                        invoiceItemsResponseDTO.setShortDesc("success");
                        invoiceItemsResponseDTO.setDescription("");
                        invoiceItemsResponseDTO.setBillNumber(invoice.getAccountId().toString());
                        break;
                    case Constants.VISA:
                        log.info("CC payment method");
                        break;
                    default:
                        log.info("Cash payment method");

                }
            } else {
                throw new TbsRunTimeException("Unknown payment method");
            }
        }else {
            invoiceItemsResponseDTO.setShortDesc("error");
            invoiceItemsResponseDTO.setDescription("error_message");
            invoice.setStatus(InvoiceStatus.FAILED);
            throw new TbsRunTimeException("Wrong invoice amount");
        }

        return invoiceItemsResponseDTO;

    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Invoice creatNewInvoice(InvoiceDTO invoiceDTO) {
        // Client
        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client =  clientService.getClientByClientId(appName);
        // get bill seq
        Long seq = sequenceUtil.getNextInvoiceNumber(client.get().getClientId());

        // Customer check if exists else create new
        Optional<Customer> customer = customerService.findByIdentifier(invoiceDTO.getCustomer().getIdentity());
        if (!customer.isPresent()) {
            customer = Optional.of(Customer.builder()
                .identity(invoiceDTO.getCustomer().getIdentity())
                // .identityType(IdentityType.valueOf(oneItemInvoiceDTO.getCustomerIdType().toUpperCase()))
                .name(invoiceDTO.getCustomer().getName())
                .contact(Contact.builder().email(invoiceDTO.getCustomer().getEmail()).phone(invoiceDTO.getCustomer().getPhone()).build())
                .build());
        }

        List<InvoiceItem> invoiceItemList = new ArrayList<InvoiceItem>();
        Invoice invoice = Invoice.builder()
            .accountId(seq + Constants.CLIENT_SADAD_CONFIG.valueOf(client.get().getClientId().toString()).getInitialAccountId())
            .number(seq + Constants.CLIENT_SADAD_CONFIG.valueOf(client.get().getClientId().toString()).getInitialBillId())
            .client(client.get())
            .customer(customer.get())
            .paymentStatus(PaymentStatus.PENDING)
            .notificationStatus(NotificationStatus.PAYMENT_NOTIFICATION_NEW)
            .status(InvoiceStatus.NEW)
            .invoiceItems(invoiceItemList)
            .build();

        BigDecimal subTotalInvoice = BigDecimal.ZERO;
        //invoiceItem
        BigDecimal totalPriceInvoice = BigDecimal.ZERO;

        for(InvoiceItemDTO invoiceItemDTO : invoiceDTO.getInvoiceItems()){

            Optional<Item> item = itemService.findByNameAndClient(invoiceItemDTO.getName(), client.get().getId());
            if (!item.isPresent()) {
                throw new TbsRunTimeException("Unknown item: "+ invoiceItemDTO.getName());
            }
            InvoiceItem invoiceItem= InvoiceItem.builder()
                .item(item.get())
                .amount(item.get().getPrice())
                .name(item.get().getName())
                .description(item.get().getDescription())
                .quantity(invoiceItemDTO.getQuantity())
                .build();

            BigDecimal totalInvoiceItem = invoiceItem.getAmount().multiply(new BigDecimal(invoiceItem.getQuantity()));
            BigDecimal discountValue = BigDecimal.ZERO;
            //Add sub discount for each item invoice  :
            if(invoiceItemDTO.getDiscount().getValue().compareTo(BigDecimal.ZERO)>0){
                if(invoiceItemDTO.getDiscount().getIsPercentage() == false){
                    Discount discount = Discount.builder().isPercentage(false).type(DiscountType.ITEM).value(invoiceItemDTO.getDiscount().getValue()).build();
                    invoiceItem.setDiscount(discount);
                     discountValue = invoiceItemDTO.getDiscount().getValue().multiply(new BigDecimal(invoiceItemDTO.getQuantity()));
                    totalInvoiceItem = totalInvoiceItem.subtract(discountValue);
                }
                // Percentage Discount
                else{
                    Discount discount = Discount.builder().isPercentage(true).type(DiscountType.ITEM).value(invoiceItemDTO.getDiscount().getValue()).build();
                    invoiceItem.setDiscount(discount);
                    BigDecimal discountRate = invoiceItemDTO.getDiscount().getValue().divide(new BigDecimal("100"));
                    discountValue =totalInvoiceItem.multiply(discountRate);
                    totalInvoiceItem= totalInvoiceItem.subtract(totalInvoiceItem.multiply(discountRate));


                }
            }
            //Add total discount
            if(invoiceDTO.getDiscount().getValue().compareTo(BigDecimal.ZERO) >0){
                if(invoiceDTO.getDiscount().getIsPercentage() == false){
                    Discount discount = Discount.builder().isPercentage(false).type(DiscountType.ITEM).value(invoiceDTO.getDiscount().getValue()).build();
                    invoice.setDiscount(discount);
                    totalInvoiceItem = totalInvoiceItem.subtract(invoice.getDiscount().getValue().multiply(new BigDecimal(invoiceItemDTO.getQuantity())));

                }else{
                    Discount discount = Discount.builder().isPercentage(true).type(DiscountType.ITEM).value(invoiceDTO.getDiscount().getValue()).build();
                    invoice.setDiscount(discount);
                    BigDecimal discountRate = invoice.getDiscount().getValue().divide(new BigDecimal("100"));
                    discountValue =totalInvoiceItem.multiply(discountRate);
                    totalInvoiceItem = totalInvoiceItem.subtract(discountValue);
                }

            }

            BigDecimal totalTaxes = BigDecimal.ZERO;
            // Adding vat
            if (CollectionUtils.isNotEmpty(item.get().getTaxes())) {
                invoiceItem.setTaxName("total_tax");
                for (Tax tax : item.get().getTaxes()) {
                    if (tax.getRate().compareTo(BigDecimal.ZERO) > 0 ) {
                        totalTaxes= totalTaxes.add(tax.getRate());
                    }
                }
                invoiceItem.setTaxRate(totalTaxes);

                BigDecimal taxRate = invoiceItem.getTaxRate().divide(new BigDecimal("100"));
                BigDecimal taxesValue = totalInvoiceItem.multiply(taxRate);
                totalInvoiceItem = totalInvoiceItem.add(taxesValue);
            }
            invoiceItemList.add(invoiceItem);
            subTotalInvoice =subTotalInvoice.add(invoiceItem.getAmount().multiply(new BigDecimal(invoiceItem.getQuantity())));
            totalPriceInvoice = totalPriceInvoice.add(totalInvoiceItem);
        }

        invoice.setInvoiceItems(invoiceItemList);
        invoice.setAmount(totalPriceInvoice);
        invoice.setSubtotal(subTotalInvoice);
        invoice.setNotificationStatus(NotificationStatus.PAYMENT_NOTIFICATION_NEW);
        invoice.setStatus(InvoiceStatus.NEW);
        invoice.setPaymentStatus(PaymentStatus.PENDING);
        return invoiceRepository.saveAndFlush(invoice);
    }


    public OneItemInvoiceRespDTO saveOneItemInvoiceAndSendEvent(OneItemInvoiceDTO oneItemInvoiceDTO) {
        TBSEventReqDTO<OneItemInvoiceDTO> reqNotification = TBSEventReqDTO.<OneItemInvoiceDTO>builder().principalId(oneItemInvoiceDTO.getCustomerId())
            .req(oneItemInvoiceDTO).build();
        return eventPublisherService.saveOneItemInvoiceEvent(reqNotification).getResp();
    }

    public OneItemInvoiceRespDTO saveOneItemInvoice(OneItemInvoiceDTO oneItemInvoiceDTO) {

        Invoice invoice = addNewInvoice(oneItemInvoiceDTO);
        // Payment
        // Payment method
        Optional<PaymentMethod> paymentMethod = paymentMethodService.findById(oneItemInvoiceDTO.getPaymentMethodId());

        OneItemInvoiceRespDTO oneItemInvoiceRespDTO= OneItemInvoiceRespDTO.builder()
            .paymentMethod(oneItemInvoiceDTO.getPaymentMethodId()).build();
        if (paymentMethod.isPresent()) {
            String paymentMethodCode = paymentMethod.get().getCode();
            switch (paymentMethodCode) {
                case Constants.SADAD:
                    int sadadResult;
                    try {
                        sadadResult = paymentService.sendEventAndCallSadad(invoice.getNumber(), invoice.getAccountId().toString(), invoice.getAmount(), oneItemInvoiceDTO.getCustomerId());
                    } catch (IOException | JSONException e) {
                        // ToDo add new exception 500 for sadad
                        throw new TbsRunTimeException("Sadad issue", e);
                    }
                    // ToDo add new exception 500 for sadad
                    // invoice = invoiceRepository.getOne(invoice.getId());
                    if (sadadResult != 200) {
                        oneItemInvoiceRespDTO.setStatusId(sadadResult);
                        oneItemInvoiceRespDTO.setShortDesc("error");
                        oneItemInvoiceRespDTO.setDescription("error_message");
                        invoice.setStatus(InvoiceStatus.FAILED);
                        // invoiceRepository.save(invoice);
                        throw new TbsRunTimeException("Sadad bill creation error");
                    }
                    invoice.setStatus(InvoiceStatus.CREATED);
                    // ToDO save again issue
                    //invoiceRepository.save(invoice);
                    oneItemInvoiceRespDTO.setStatusId(1);
                    oneItemInvoiceRespDTO.setShortDesc("success");
                    oneItemInvoiceRespDTO.setDescription("");
                    oneItemInvoiceRespDTO.setBillNumber(invoice.getAccountId().toString());
                    break;
                case Constants.VISA:
                    log.info("CC payment method");
                break;
                default:
                    log.info("Cash payment method");

            }
        } else {
            throw new TbsRunTimeException("Unknown payment method");
        }

        return oneItemInvoiceRespDTO;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Invoice addNewInvoice(OneItemInvoiceDTO oneItemInvoiceDTO) {
        // Client
        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client =  clientService.getClientByClientId(appName);

        // get bill seq
        Long seq = sequenceUtil.getNextInvoiceNumber(client.get().getClientId());

        // Customer check if exists else create new
        Optional<Customer> customer = customerService.findByIdentifier(oneItemInvoiceDTO.getCustomerId());
        if (!customer.isPresent()) {
            customer = Optional.of(Customer.builder()
                .identity(oneItemInvoiceDTO.getCustomerId())
                // .identityType(IdentityType.valueOf(oneItemInvoiceDTO.getCustomerIdType().toUpperCase()))
                .name(oneItemInvoiceDTO.getCustomerName())
                .contact(Contact.builder().email(oneItemInvoiceDTO.getEmail()).phone(oneItemInvoiceDTO.getMobile()).build())
                .build());
        }

        Invoice invoice = Invoice.builder()
            .accountId(seq + Constants.CLIENT_SADAD_CONFIG.valueOf(client.get().getClientId().toString()).getInitialAccountId())
            .number(seq + Constants.CLIENT_SADAD_CONFIG.valueOf(client.get().getClientId().toString()).getInitialBillId())
            .client(client.get())
            .customer(customer.get())
            .paymentStatus(PaymentStatus.PENDING)
            .status(InvoiceStatus.NEW)
            .build();

        //invoiceItem
        Optional<Item> item = itemService.findByNameAndClient(oneItemInvoiceDTO.getItemName(), client.get().getId());
        if (!item.isPresent()) {
            throw new TbsRunTimeException("Unknown item: "+ oneItemInvoiceDTO.getItemName());
        }
        InvoiceItem invoiceItem = InvoiceItem.builder()
            .item(item.get())
            .amount(item.get().getPrice())
            .name(item.get().getName())
            .description(item.get().getDescription())
            .quantity(oneItemInvoiceDTO.getQuantity())
            .invoice(invoice)
            .build();


        BigDecimal totalPrice = null;

        //Add Discount if price <> item
        if (oneItemInvoiceDTO.getPrice() != null) {
            if (oneItemInvoiceDTO.getPrice().compareTo(item.get().getPrice().multiply(BigDecimal.valueOf(oneItemInvoiceDTO.getQuantity()))) < 0) {
                BigDecimal discountAmount = item.get().getPrice().multiply(new BigDecimal(oneItemInvoiceDTO.getQuantity())).subtract(oneItemInvoiceDTO.getPrice());
                Discount discount = Discount.builder().isPercentage(false).type(DiscountType.ITEM).value(discountAmount).build();
                invoiceItem.setDiscount(discount);
                totalPrice = oneItemInvoiceDTO.getPrice();
            }
        } else {
            totalPrice = item.get().getPrice();
            invoiceItem.setQuantity(1);
        }

        // adding vat
        BigDecimal totalTaxes = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(item.get().getTaxes())) {
            for (Tax tax: item.get().getTaxes()) {
                if (tax.getRate() != null) {
                    totalPrice = totalPrice.add(item.get().getPrice().multiply(tax.getRate().divide(new BigDecimal("100"))));
                    totalTaxes = totalTaxes.add(tax.getRate());
                }
            }
        }
        // Add tax to invoice Item ToDo
        invoiceItem.setTaxName("Total_Taxes");
        invoiceItem.setTaxRate(totalTaxes);

        invoice.setInvoiceItems(Arrays.asList(invoiceItem));
        invoice.setSubtotal(item.get().getPrice());
        invoice.setAmount(totalPrice);
        return invoiceRepository.saveAndFlush(invoice);
    }


    public InvoiceStatusDTO getOneItemInvoice(Long billNumber) {
        // Optional<Invoice> invoice = invoiceRepository.findById(billNumber-7000000065l);
        Optional<Invoice> invoice = invoiceRepository.findByAccountId(billNumber);
        if (!invoice.isPresent()) {
            throw new TbsRunTimeException("Bill does not exist");
        }
        InvoiceItem invoiceItem = invoice.get().getInvoiceItems().get(0);
        String issueDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date.from(invoice.get().getCreatedDate().toInstant()));
        return InvoiceStatusDTO.builder()
            .billNumber(billNumber.toString())
            .vat(invoice.get().getAmount().subtract(invoice.get().getSubtotal()))
            .vatNumber("300879111900003")
            .price(invoice.get().getSubtotal())
            .itemName(invoiceItem.getItem().getName())
            .quantity(1)
            .billerId(156)
            .companyName("تمكين للتقنيات")
            .issueDate(issueDate)
            .build();
    }

    public DataTablesOutput<InvoiceDTO> get(DataTablesInput input) {
        return invoiceMapper.toDto(invoiceRepository.findAll(input));
    }

    public List<Object[]> getMonthlyStat( ZonedDateTime lastDate) {
        ZonedDateTime first = lastDate.withDayOfMonth(1);
        YearMonth yearMonthObject = YearMonth.of(lastDate.getYear(), lastDate.getMonth());
        ZonedDateTime last = lastDate.withDayOfMonth( yearMonthObject.lengthOfMonth());
        List<Object[]> stats = invoiceRepository.getStatisticsByMonth(first, last);
        return stats;
    }

    public List<Object[]> getAnnualyStat( ZonedDateTime lastDate) {
        ZonedDateTime first = lastDate.withMonth(1).withDayOfMonth(1);
        YearMonth yearMonthObject = YearMonth.of(lastDate.getYear(), lastDate.getMonth());
        ZonedDateTime last = lastDate.withMonth(12).withDayOfMonth(31);
        List<Object[]> stats = invoiceRepository.getStatisticsByYear(first, last);
        return stats;
    }

}
