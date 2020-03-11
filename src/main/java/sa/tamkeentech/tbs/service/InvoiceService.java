package sa.tamkeentech.tbs.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.domain.enumeration.*;
import sa.tamkeentech.tbs.repository.CustomerRepository;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.repository.PersistenceAuditEventRepository;
import sa.tamkeentech.tbs.security.SecurityUtils;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.service.mapper.InvoiceMapper;
import sa.tamkeentech.tbs.service.mapper.PaymentMethodMapper;
import sa.tamkeentech.tbs.service.util.EventPublisherService;
import sa.tamkeentech.tbs.service.util.LanguageUtil;
import sa.tamkeentech.tbs.service.util.SequenceUtil;
import sa.tamkeentech.tbs.web.rest.errors.PaymentGatewayException;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Invoice}.
 */
@Service
// To Solve invoice not persisted issue remove this transactional
// But need to make the save logic bloc transactioal --> issue of existing customer -> detached ...
// @Transactional
public class InvoiceService {

    private final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    public final static String INVOICE_BY_ACCOUNT_ID = "invoiceByAccountId";

    private final InvoiceRepository invoiceRepository;

    private final InvoiceMapper invoiceMapper;

    private final ClientService clientService;

    private final CustomerService customerService;

    private final PaymentMethodService paymentMethodService;

    private final ItemService itemService;

    private final CustomerRepository customerRepository;

    private final PaymentService paymentService;

    private final SequenceUtil sequenceUtil;

    private final EventPublisherService eventPublisherService;
    private final EntityManager entityManager;
    private final PaymentMethodMapper paymentMethodMapper;

    private final PersistenceAuditEventRepository persistenceAuditEventRepository;

    private final LanguageUtil languageUtil;

    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private PaymentRepository paymentRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper, ClientService clientService, CustomerService customerService, PaymentMethodService paymentMethodService, ItemService itemService, PaymentService paymentService, SequenceUtil sequenceUtil, EventPublisherService eventPublisherService, CustomerRepository customerRepository, EntityManager entityManager, PaymentMethodMapper paymentMethodMapper, PersistenceAuditEventRepository persistenceAuditEventRepository, LanguageUtil languageUtil) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.customerService = customerService;
        this.paymentMethodService = paymentMethodService;
        this.itemService = itemService;
        this.paymentService = paymentService;
        this.sequenceUtil = sequenceUtil;
        this.eventPublisherService = eventPublisherService;
        this.customerRepository = customerRepository;
        this.entityManager = entityManager;
        this.paymentMethodMapper = paymentMethodMapper;
        this.clientService = clientService;

        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
        this.languageUtil = languageUtil;
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

    @Cacheable(value = INVOICE_BY_ACCOUNT_ID)
    public Optional<InvoiceDTO> findByAccountId(Long id) {
        log.debug("Request to get Invoice by findByAccountId : {}", id);
        return invoiceRepository.findByAccountId(id)
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

    public InvoiceResponseDTO saveInvoiceAndSendEvent(InvoiceDTO invoiceDTO) {
        TBSEventReqDTO<InvoiceDTO> reqNotification = TBSEventReqDTO.<InvoiceDTO>builder().principalId(invoiceDTO.getCustomer().getIdentity())
            .req(invoiceDTO).build();
        return eventPublisherService.saveInvoiceEvent(reqNotification).getResp();
    }

    public InvoiceResponseDTO saveInvoice(InvoiceDTO invoiceDTO) {

        Invoice invoice = createNewInvoice(invoiceDTO);

        // Payment
        // Payment method
        Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(invoiceDTO.getPaymentMethod().getCode());

        InvoiceResponseDTO invoiceItemsResponseDTO = InvoiceResponseDTO.builder()
            .paymentMethod(invoiceDTO.getPaymentMethod().getId()).build();

        if (paymentMethod.isPresent()) {
            String paymentMethodCode = paymentMethod.get().getCode();
            InvoiceStatus status;
            switch (paymentMethodCode) {
                case Constants.SADAD:
                    int sadadResult;
                    try {
                        sadadResult = paymentService.sendEventAndCallSadad(invoice.getNumber(), invoice.getAccountId().toString(), invoice.getAmount(), invoiceDTO.getCustomer().getIdentity());
                    } catch (IOException | JSONException e) {
                        throw new PaymentGatewayException("Sadad issue");
                    }
                    if (sadadResult != 200) {
                        throw new PaymentGatewayException("Sadad bill creation error");
                    }
                    break;
                case Constants.CREDIT_CARD:
                    invoiceItemsResponseDTO.setLink(paymentService.savePaymentAndGetPaymentUrl(invoice, paymentMethod.get()));
                    log.debug("CC payment method");
                    break;
                default:
                    log.info("Cash payment method");
                    break;
            }
            status = InvoiceStatus.CREATED;
            updateInvoice(invoice.getId(), status);
            invoiceItemsResponseDTO.setStatusId(1);
            invoiceItemsResponseDTO.setShortDesc("success");
            invoiceItemsResponseDTO.setDescription("");
            invoiceItemsResponseDTO.setBillNumber(invoice.getAccountId().toString());
        } else {
            throw new TbsRunTimeException("Unknown payment method");
        }
        return invoiceItemsResponseDTO;

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = TbsRunTimeException.class)
    Invoice createNewInvoice(InvoiceDTO invoiceDTO) {
        // Client
        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client = clientService.getClientByClientId(appName);

        // check if Customer Type not null
        if (invoiceDTO.getCustomer().getIdentityType() == null) {
            throw new TbsRunTimeException("Customer Type is mandatory");
        }
        // Customer check if exists else create new
        Optional<Customer> customer = customerService.findByIdentifier(invoiceDTO.getCustomer().getIdentity());
        if (!customer.isPresent()) {
            customer = Optional.of(Customer.builder()
                .identity(invoiceDTO.getCustomer().getIdentity())
                .identityType(invoiceDTO.getCustomer().getIdentityType())
                .name(invoiceDTO.getCustomer().getName())
                .contact(Contact.builder().email(invoiceDTO.getCustomer().getEmail()).phone(invoiceDTO.getCustomer().getPhone()).build())
                .build());
            customerRepository.save(customer.get());
        }

        List<InvoiceItem> invoiceItemList = new ArrayList<InvoiceItem>();
        Invoice invoice = Invoice.builder()
            .client(client.get())
            .customer(customer.get())
            .paymentStatus(PaymentStatus.PENDING)
            .status(InvoiceStatus.NEW)
            .invoiceItems(invoiceItemList)
            .build();

        BigDecimal subTotalInvoice = BigDecimal.ZERO;
        //invoiceItem
        BigDecimal totalPriceInvoice = BigDecimal.ZERO;

        // Net item price N = Item price I * Quantity Q
        // Tax multiplier T = 1 + tax rate (1 + 0.05)
        // Average Tax T= (N1*T1 + N2*T2 + ...) / (N1 + N2 + ...)
        BigDecimal avgTax = BigDecimal.ZERO;
        BigDecimal avgTaxNumerator = BigDecimal.ZERO;
        BigDecimal avgTaxDenominator = BigDecimal.ZERO;
        for (InvoiceItemDTO invoiceItemDTO : invoiceDTO.getInvoiceItems()) {
            Optional<Item> item = itemService.findByCodeAndClient(invoiceItemDTO.getItemCode(), client.get().getId());
            if (!item.isPresent()) {
                throw new TbsRunTimeException("Unknown item: " + invoiceItemDTO.getItemCode());
            }
            InvoiceItem invoiceItem = InvoiceItem.builder()
                .item(item.get())
                .name(item.get().getName())
                .code(item.get().getCode())
                .quantity(invoiceItemDTO.getQuantity())
                .invoice(invoice)
                .build();
            if (item.get().isFlexiblePrice()) {
                if (StringUtils.isEmpty(invoiceItemDTO.getDetails()) || invoiceItemDTO.getAmount() == null || invoiceItemDTO.getAmount().equals(BigDecimal.ZERO)) {
                    throw new TbsRunTimeException("Item amount and details are mandatory");
                } else {
                    invoiceItem.setAmount(invoiceItemDTO.getAmount());
                }
            } else {
                invoiceItem.setAmount(item.get().getPrice());
            }
            invoiceItem.setDetails(invoiceItemDTO.getDetails());
            invoiceItem.setArguments(invoiceItemDTO.getArguments());

            BigDecimal totalInvoiceItem = invoiceItem.getAmount().multiply(new BigDecimal(invoiceItem.getQuantity()));
            // Add sub discount for each item invoice  :
            // Item discount is Tax exclusive -> apply discount then add tax
            if (invoiceItemDTO.getDiscount() != null && invoiceItemDTO.getDiscount().getValue().compareTo(BigDecimal.ZERO) > 0) {
                if (invoiceItemDTO.getDiscount().getIsPercentage() == false) {
                    if (invoiceItemDTO.getDiscount().getValue().compareTo(invoiceItem.getAmount()) > 0) {
                        throw new TbsRunTimeException("Wrong discount value");
                    }
                    BigDecimal discountValue = BigDecimal.ZERO;
                    Discount discount = Discount.builder().isPercentage(false).type(DiscountType.ITEM).value(invoiceItemDTO.getDiscount().getValue()).build();
                    invoiceItem.setDiscount(discount);
                    discountValue = invoiceItemDTO.getDiscount().getValue().multiply(new BigDecimal(invoiceItemDTO.getQuantity()));
                    totalInvoiceItem = totalInvoiceItem.subtract(discountValue);
                }
                // Percentage Discount
                else {
                    if (invoiceItemDTO.getDiscount().getValue().compareTo(new BigDecimal("100")) > 0) {
                        throw new TbsRunTimeException("Wrong discount value");
                    }
                    Discount discount = Discount.builder().isPercentage(true).type(DiscountType.ITEM).value(invoiceItemDTO.getDiscount().getValue()).build();
                    invoiceItem.setDiscount(discount);
                    BigDecimal discountRate = invoiceItemDTO.getDiscount().getValue().divide(new BigDecimal("100"));
                    // discountValue = totalInvoiceItem.multiply(discountRate);
                    totalInvoiceItem = totalInvoiceItem.subtract(totalInvoiceItem.multiply(discountRate));
                }
            }
            //Add total discount
            /*if(invoiceDTO.getDiscount().getValue().compareTo(BigDecimal.ZERO) >0) {
                if (invoiceDTO.getDiscount().getIsPercentage() == false) {
                    Discount discount = Discount.builder().isPercentage(false).type(DiscountType.ITEM).value(invoiceDTO.getDiscount().getValue()).build();
                    invoice.setDiscount(discount);
                    totalInvoiceItem = totalInvoiceItem.subtract(invoice.getDiscount().getValue().multiply(new BigDecimal(invoiceItemDTO.getQuantity())));
                } else {
                    Discount discount = Discount.builder().isPercentage(true).type(DiscountType.ITEM).value(invoiceDTO.getDiscount().getValue()).build();
                    invoice.setDiscount(discount);
                    BigDecimal discountRate = invoice.getDiscount().getValue().divide(new BigDecimal("100"));
                    discountValue =totalInvoiceItem.multiply(discountRate);
                    totalInvoiceItem = totalInvoiceItem.subtract(discountValue);
                }
            }*/
            avgTaxDenominator = avgTaxDenominator.add(totalInvoiceItem);
            BigDecimal totalTaxes = BigDecimal.ZERO;
            // Adding vat
            if (CollectionUtils.isNotEmpty(item.get().getTaxes())) {
                invoiceItem.setTaxName("total_tax");
                for (Tax tax : item.get().getTaxes()) {
                    if (tax.getRate().compareTo(BigDecimal.ZERO) > 0) {
                        totalTaxes = totalTaxes.add(tax.getRate());
                    }
                }
                invoiceItem.setTaxRate(totalTaxes);

                BigDecimal taxRate = invoiceItem.getTaxRate().divide(new BigDecimal("100"));
                BigDecimal taxesValue = totalInvoiceItem.multiply(taxRate);
                totalInvoiceItem = totalInvoiceItem.add(taxesValue);
            }
            avgTaxNumerator = avgTaxNumerator.add(totalInvoiceItem);
            invoiceItemList.add(invoiceItem);
            // subTotalInvoice =subTotalInvoice.add(invoiceItem.getAmount().multiply(new BigDecimal(invoiceItem.getQuantity())));
            totalPriceInvoice = totalPriceInvoice.add(totalInvoiceItem);
        }

        // Invoice discount is Tax inclusive  -> add tax then apply discount
        if (invoiceDTO.getDiscount() != null && invoiceDTO.getDiscount().getValue().compareTo(BigDecimal.ZERO) > 0) {
            if (invoiceDTO.getDiscount().getIsPercentage() == false) {
                Discount discount = Discount.builder().isPercentage(false).type(DiscountType.INVOICE).value(invoiceDTO.getDiscount().getValue()).build();
                invoice.setDiscount(discount);
                totalPriceInvoice = totalPriceInvoice.subtract(invoice.getDiscount().getValue());
            } else {
                Discount discount = Discount.builder().isPercentage(true).type(DiscountType.INVOICE).value(invoiceDTO.getDiscount().getValue()).build();
                invoice.setDiscount(discount);
                BigDecimal discountRate = invoice.getDiscount().getValue().divide(new BigDecimal("100"));
                BigDecimal discountValue = totalPriceInvoice.multiply(discountRate);
                totalPriceInvoice = totalPriceInvoice.subtract(discountValue);
            }
        }
        if (totalPriceInvoice.compareTo(BigDecimal.ZERO) < 0) {
            throw new TbsRunTimeException("Wrong discount value");
        }

        // Check based on passed values in case of flexible price
        if (invoiceDTO.getAmount()!= null && (invoiceDTO.getAmount().compareTo(totalPriceInvoice.setScale(2, RoundingMode.HALF_UP)) != 0
            || invoiceDTO.getAmount().compareTo(BigDecimal.ZERO) < 0)) {
            throw new TbsRunTimeException("Wrong invoice amount");
        }
        // calculate average tax
        if (avgTaxDenominator.compareTo(BigDecimal.ZERO) > 0) {
            avgTax = avgTaxNumerator.divide(avgTaxDenominator, RoundingMode.HALF_UP);
            subTotalInvoice = totalPriceInvoice.divide(avgTax, RoundingMode.HALF_UP);
        }
        // get bill seq
        Long seq = sequenceUtil.getNextInvoiceNumber(client.get().getClientId());
        if (seq == null || client.get().getInitialAccountId() == null || client.get().getInitialBillId() == null) {
            throw new TbsRunTimeException("Unable to get invoice number.");
        }
        invoice.setAccountId(seq + client.get().getInitialAccountId());
        invoice.setNumber(seq + client.get().getInitialBillId());
        invoice.setInvoiceItems(invoiceItemList);
        invoice.setAmount(totalPriceInvoice);
        invoice.setSubtotal(subTotalInvoice);
        invoice.setTaxFees(totalPriceInvoice.subtract(subTotalInvoice));
        // Now
        invoice.setDueDate(ZonedDateTime.now());
        // default expiry date: Now + 1 days
        if(invoiceDTO.getExpirationDays() != null  && invoiceDTO.getExpirationDays() > 0)
            invoice.setExpiryDate(ZonedDateTime.now().plusDays(invoiceDTO.getExpirationDays()));
        else
            invoice.setExpiryDate(ZonedDateTime.now().plusDays(Constants.INVOICE_EXPIRY_DAYS));

        invoice.setStatus(InvoiceStatus.NEW);
        invoice.setPaymentStatus(PaymentStatus.PENDING);
        return invoiceRepository.saveAndFlush(invoice);
    }


    public InvoiceResponseDTO saveOneItemInvoiceAndSendEvent(OneItemInvoiceDTO oneItemInvoiceDTO) {
        TBSEventReqDTO<OneItemInvoiceDTO> reqNotification = TBSEventReqDTO.<OneItemInvoiceDTO>builder().principalId(oneItemInvoiceDTO.getCustomerId())
            .req(oneItemInvoiceDTO).build();
        return eventPublisherService.saveOneItemInvoiceEvent(reqNotification).getResp();
    }

    public InvoiceResponseDTO saveOneItemInvoice(OneItemInvoiceDTO oneItemInvoiceDTO) {

        Invoice invoice = addNewOneItemInvoice(oneItemInvoiceDTO);
        // Payment
        // Payment method
        Optional<PaymentMethod> paymentMethod = paymentMethodService.findById(oneItemInvoiceDTO.getPaymentMethodId());

        InvoiceResponseDTO oneItemInvoiceRespDTO = InvoiceResponseDTO.builder()
            .paymentMethod(oneItemInvoiceDTO.getPaymentMethodId()).build();
        if (paymentMethod.isPresent()) {
            String paymentMethodCode = paymentMethod.get().getCode();
            InvoiceStatus status;
            switch (paymentMethodCode) {
                case Constants.SADAD:
                    int sadadResult = 0;
                    try {
                        sadadResult = paymentService.sendEventAndCallSadad(invoice.getNumber(), invoice.getAccountId().toString(), invoice.getAmount(), oneItemInvoiceDTO.getCustomerId());
                    } catch (IOException | JSONException e) {
                        // keep the old response
                        // throw new TbsRunTimeException("Sadad issue", ErrorConstants.ERR_PAYMENT_GATEWAY);
                        log.error("something went wrong with Sadad, please check out the log below: {}", e);
                    }
                    if (sadadResult != 200) {
                        oneItemInvoiceRespDTO.setStatusId(sadadResult);
                        oneItemInvoiceRespDTO.setShortDesc("error");
                        oneItemInvoiceRespDTO.setDescription("error_message");
                        status = InvoiceStatus.FAILED;
                        updateInvoice(invoice.getId(), status);
                        // keep the old response
                        // throw new TbsRunTimeException("Sadad bill creation error", ErrorConstants.ERR_PAYMENT_GATEWAY);
                        return oneItemInvoiceRespDTO;
                    }
                    break;
                case Constants.CREDIT_CARD:
                    log.debug("CC payment method");
                    /*BigDecimal roundedAmount = invoice.getAmount().setScale(2, RoundingMode.HALF_UP);
                    String appCode = invoice.getClient().getPaymentKeyApp();
                    PaymentResponseDTO paymentResponseDTO = null;
                    try {
                        paymentResponseDTO = paymentService.sendEventAndCreditCardCall(Optional.of(invoice), appCode, roundedAmount.multiply(new BigDecimal("100")));
                    } catch (JSONException | IOException e) {
                        throw new PaymentGatewayException("Payment gateway issue: " + e.getCause());
                    }*/
                    oneItemInvoiceRespDTO.setLink(paymentService.savePaymentAndGetPaymentUrl(invoice, paymentMethod.get()));
                    break;
                default:
                    log.info("Cash payment method");

            }
            status = InvoiceStatus.CREATED;
            updateInvoice(invoice.getId(), status);
            oneItemInvoiceRespDTO.setStatusId(1);
            oneItemInvoiceRespDTO.setShortDesc("success");
            oneItemInvoiceRespDTO.setDescription("");
            oneItemInvoiceRespDTO.setBillNumber(invoice.getAccountId().toString());
        } else {
            throw new TbsRunTimeException("Unknown payment method");
        }

        return oneItemInvoiceRespDTO;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = TbsRunTimeException.class)
    public Invoice addNewOneItemInvoice(OneItemInvoiceDTO oneItemInvoiceDTO) {
        // Client
        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client = clientService.getClientByClientId(appName);

        // Customer check if exists else create new
        Optional<Customer> customer = customerService.findByIdentifier(oneItemInvoiceDTO.getCustomerId());
        if (!customer.isPresent()) {
            customer = Optional.of(Customer.builder()
                .identity(oneItemInvoiceDTO.getCustomerId())
                // .identityType(IdentityType.valueOf(oneItemInvoiceDTO.getCustomerIdType().toUpperCase()))
                .name(oneItemInvoiceDTO.getCustomerName())
                .contact(Contact.builder().email(oneItemInvoiceDTO.getEmail()).phone(oneItemInvoiceDTO.getMobile()).build())
                .build());
            if (StringUtils.isNotEmpty(oneItemInvoiceDTO.getCustomerIdType()) && oneItemInvoiceDTO.getCustomerIdType().startsWith(IdentityType.IQA.name())) {
                customer.get().setIdentityType(IdentityType.IQA);
            } else if (StringUtils.isNotEmpty(oneItemInvoiceDTO.getCustomerIdType()) && oneItemInvoiceDTO.getCustomerIdType().startsWith(IdentityType.NAT.name())) {
                customer.get().setIdentityType(IdentityType.NAT);
            } else if (oneItemInvoiceDTO.getCustomerId().startsWith("2")) {
                customer.get().setIdentityType(IdentityType.IQA);
            } else {
                customer.get().setIdentityType(IdentityType.NAT);
            }
            customerRepository.save(customer.get());
        }

        //invoiceItem
        Optional<Item> item = itemService.findByCodeAndClient(oneItemInvoiceDTO.getItemName(), client.get().getId());
        if (!item.isPresent()) {
            throw new TbsRunTimeException("Unknown item: " + oneItemInvoiceDTO.getItemName());
        }

        // get bill seq
        Long seq = sequenceUtil.getNextInvoiceNumber(client.get().getClientId());
        if (seq == null || client.get().getInitialAccountId() == null || client.get().getInitialBillId() == null) {
            throw new TbsRunTimeException("Unable to get invoice number.");
        }
        Invoice invoice = Invoice.builder()
            .accountId(seq + client.get().getInitialAccountId())
            .number(seq + client.get().getInitialBillId())
            .client(client.get())
            .customer(customer.get())
            .paymentStatus(PaymentStatus.PENDING)
            .status(InvoiceStatus.NEW)
            .build();

        InvoiceItem invoiceItem = InvoiceItem.builder()
            .item(item.get())
            .amount(item.get().getPrice())
            .name(item.get().getName())
            .code(item.get().getCode())
            .quantity(oneItemInvoiceDTO.getQuantity())
            .invoice(invoice)
            .build();


        BigDecimal totalPrice = BigDecimal.ZERO;

        //Add Discount if price <> item
        if (oneItemInvoiceDTO.getPrice() != null) {
            if (oneItemInvoiceDTO.getPrice().compareTo(item.get().getPrice().multiply(BigDecimal.valueOf(oneItemInvoiceDTO.getQuantity()))) < 0) {
                BigDecimal discountAmount = item.get().getPrice().multiply(new BigDecimal(oneItemInvoiceDTO.getQuantity())).subtract(oneItemInvoiceDTO.getPrice());
                Discount discount = Discount.builder().isPercentage(false).type(DiscountType.ITEM).value(discountAmount).build();
                invoiceItem.setDiscount(discount);
                totalPrice = oneItemInvoiceDTO.getPrice();
            } else {
                totalPrice = item.get().getPrice();
                invoiceItem.setQuantity(1);
            }
        } else {
            totalPrice = item.get().getPrice();
            invoiceItem.setQuantity(1);
        }

        // adding vat
        BigDecimal totalTaxes = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(item.get().getTaxes())) {
            for (Tax tax : item.get().getTaxes()) {
                if (tax.getRate() != null) {
                    totalPrice = totalPrice.add(totalPrice.multiply(tax.getRate().divide(new BigDecimal("100"))));
                    totalTaxes = totalTaxes.add(tax.getRate());
                }
            }
        }
        invoiceItem.setTaxName("Total_Taxes");
        invoiceItem.setTaxRate(totalTaxes);

        invoice.setInvoiceItems(Arrays.asList(invoiceItem));
        invoice.setSubtotal(item.get().getPrice());
        invoice.setAmount(totalPrice);
        invoice.setTaxFees(totalPrice.subtract(item.get().getPrice()));
        invoice.setStatus(InvoiceStatus.NEW);
        invoice.setPaymentStatus(PaymentStatus.PENDING);
        // Now
        invoice.setDueDate(ZonedDateTime.now());
        // Now + 2days
        invoice.setExpiryDate(ZonedDateTime.now().plusDays(Constants.INVOICE_EXPIRY_DAYS));
        return invoiceRepository.saveAndFlush(invoice);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateInvoice(Long invoiceId, InvoiceStatus status) {
        return invoiceRepository.setStatus(invoiceId, status);
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
            .itemName(invoiceItem.getItem().getCode())
            .quantity(1)
            .billerId(156)
            .companyName("تمكين للتقنيات")
            .issueDate(issueDate)
            .build();
    }

    public DataTablesOutput<InvoiceDTO> get(DataTablesInput input) {
        return invoiceMapper.toDto(invoiceRepository.findAll(input, (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            List<Long> clientIds = userService.listClientIds(null);
            predicates.add(criteriaBuilder.and(root.get("client").get("id").in(clientIds)));
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }));
    }

    @Transactional(readOnly = true)
    public DataTablesOutput<InvoiceDTO> getInvoiceByQuerySearch(InvoiceSearchRequestDTO invoiceSearchRequestDTO) {
        // return itemMapper.toDto(itemRepository.findAll(input));
        return invoiceMapper.toDto(invoiceRepository.findAll(invoiceSearchRequestDTO.getInput(), (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            List<Long> clientIds = userService.listClientIds(invoiceSearchRequestDTO.getClientId());
            predicates.add(criteriaBuilder.and(root.get("client").get("id").in(clientIds)));

            if (invoiceSearchRequestDTO.getFromDate() != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), invoiceSearchRequestDTO.getFromDate())));
            }
            if (invoiceSearchRequestDTO.getToDate() != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), invoiceSearchRequestDTO.getToDate())));
            }
            if (StringUtils.isNotEmpty(invoiceSearchRequestDTO.getCustomerId())) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("customer").get("identity"), invoiceSearchRequestDTO.getCustomerId())));
            }
//            if (!invoiceSearchRequestDTO.getPaymentStatus().equals(null)) {
//                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("paymentStatus"), invoiceSearchRequestDTO.getPaymentStatus())));
//            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }));
    }

    public List<Object[]> getMonthlyStat(ZonedDateTime lastDate, long clientId) {
        ZonedDateTime first = lastDate.withDayOfMonth(1);
        YearMonth yearMonthObject = YearMonth.of(lastDate.getYear(), lastDate.getMonth());
        ZonedDateTime last = lastDate.withDayOfMonth(yearMonthObject.lengthOfMonth());
        List<Object[]> stats = invoiceRepository.getStatisticsByMonth(first, last, clientId);
        return stats;
    }

    public List<Object[]> getAnnualyStat(ZonedDateTime lastDate, long clientId) {
        ZonedDateTime first = lastDate.withMonth(1).withDayOfMonth(1);
        YearMonth yearMonthObject = YearMonth.of(lastDate.getYear(), lastDate.getMonth());
        ZonedDateTime last = lastDate.withMonth(12).withDayOfMonth(31);
        List<Object[]> stats = invoiceRepository.getStatisticsByYear(first, last, clientId);
        return stats;
    }


    @Scheduled(cron = "${tbs.cron.invoice-expired}")
    public void checkExpiredInvoice() {
    List<Invoice> invoices = invoiceRepository.getExpiryInvoices(ZonedDateTime.now());
        for(Invoice invoice : invoices){
            invoice.setStatus(InvoiceStatus.EXPIRED);
            save(invoiceMapper.toDto(invoice));
        }
    }

    public void addExtraPaymentInfo(Object invoice, String language) {
        if (invoice instanceof InvoiceDTO) {
            InvoiceDTO invoiceDTO = (InvoiceDTO)invoice;
            invoiceDTO.setVatNumber("300879111900003");
            Optional<PersistentAuditEvent> event = persistenceAuditEventRepository.findFirstByRefIdAndSuccessfulAndAuditEventTypeOrderByIdDesc(invoiceDTO.getAccountId(), true, Constants.EventType.SADAD_INITIATE.name());
            if (event.isPresent()) {
                invoiceDTO.setBillerId(156);
            }
            String lang = StringUtils.isNotEmpty(language)? language: Constants.LANGUAGE.ARABIC.getHeaderKey();
            invoiceDTO.setCompanyName(languageUtil.getMessageByKey("company.name", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        } else if (invoice instanceof InvoiceResponseDTO) {
            InvoiceResponseDTO invoiceResponseDTO = (InvoiceResponseDTO)invoice;
            invoiceResponseDTO.setVatNumber("300879111900003");
            Optional<PersistentAuditEvent> event = persistenceAuditEventRepository.findFirstByRefIdAndSuccessfulAndAuditEventTypeOrderByIdDesc(Long.parseLong(invoiceResponseDTO.getBillNumber()), true, Constants.EventType.SADAD_INITIATE.name());
            if (event.isPresent()) {
                invoiceResponseDTO.setBillerId(156);
            }
            String lang = StringUtils.isNotEmpty(language)? language: Constants.LANGUAGE.ARABIC.getHeaderKey();
            invoiceResponseDTO.setCompanyName(languageUtil.getMessageByKey("company.name", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        }
    }


    public List<InvoiceDTO> findByCustomerId(String customerId, String language) {
        return invoiceRepository.findTop1000ByCustomerIdentity(customerId).stream().map(invoice -> {
            InvoiceDTO invoiceDTO = invoiceMapper.toDto(invoice);
            invoiceDTO.setClient(null);
            addExtraPaymentInfo(invoiceDTO, language);
            return invoiceDTO;
        }).collect(Collectors.toList());
    }

}
