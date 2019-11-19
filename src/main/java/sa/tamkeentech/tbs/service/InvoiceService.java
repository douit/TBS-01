package sa.tamkeentech.tbs.service;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.domain.enumeration.*;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.security.SecurityUtils;
import sa.tamkeentech.tbs.service.dto.InvoiceDTO;
import sa.tamkeentech.tbs.service.dto.InvoiceStatusDTO;
import sa.tamkeentech.tbs.service.dto.OneItemInvoiceDTO;
import sa.tamkeentech.tbs.service.dto.OneItemInvoiceRespDTO;
import sa.tamkeentech.tbs.service.mapper.InvoiceMapper;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

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

    private final PaymentService paymentService;

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper, ClientService clientService, CustomerService customerService, PaymentMethodService paymentMethodService, ItemService itemService, PaymentService paymentService) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.clientService = clientService;
        this.customerService = customerService;
        this.paymentMethodService = paymentMethodService;
        this.itemService = itemService;
        this.paymentService = paymentService;
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
                    String billId = paymentService.getSadadBillAccount(invoice.getId()).toString();
                    int sadadResult;
                    try {
                        sadadResult = paymentService.sadadCall(paymentService.getSadadBillId(invoice.getId()), paymentService.getSadadBillAccount(invoice.getId()).toString(), invoice.getAmount());
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
                    // invoiceRepository.save(invoice);
                    oneItemInvoiceRespDTO.setStatusId(1);
                    oneItemInvoiceRespDTO.setShortDesc("success");
                    oneItemInvoiceRespDTO.setDescription("");
                    oneItemInvoiceRespDTO.setBillNumber(billId);
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
    Invoice addNewInvoice(OneItemInvoiceDTO oneItemInvoiceDTO) {
        // Client
        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client =  clientService.getClientByClientId(appName);

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
            .client(client.get())
            .customer(customer.get())
            .paymentStatus(PaymentStatus.PENDING)
            .notificationStatus(NotificationStatus.PAYMENT_NOTIFICATION_NEW)
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
                Discount discount = Discount.builder().iPercentage(false).type(DiscountType.ITEM).value(discountAmount).build();
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
        Optional<Invoice> invoice = invoiceRepository.findById(billNumber);
        if (!invoice.isPresent()) {
            throw new TbsRunTimeException("Bill does not exist");
        }
        InvoiceItem invoiceItem = invoice.get().getInvoiceItems().get(0);
        String issueDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date.from(invoice.get().getCreatedDate()));
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

}
