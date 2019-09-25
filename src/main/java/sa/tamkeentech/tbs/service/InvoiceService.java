package sa.tamkeentech.tbs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.domain.enumeration.DiscountType;
import sa.tamkeentech.tbs.domain.enumeration.IdentityType;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.security.SecurityUtils;
import sa.tamkeentech.tbs.service.dto.InvoiceDTO;
import sa.tamkeentech.tbs.service.dto.OneItemInvoiceDTO;
import sa.tamkeentech.tbs.service.mapper.InvoiceMapper;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
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

    @Transactional
    public OneItemInvoiceDTO saveOnItemInvoice(OneItemInvoiceDTO oneItemInvoiceDTO) {
        // Client
        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client =  clientService.getClientByClientId(appName);

        // Customer check if exists else create new
        Optional<Customer> customer = customerService.findByIdentifier(oneItemInvoiceDTO.getCustomerId());
        if (!customer.isPresent()) {
            customer = Optional.of(Customer.builder()
                .identity(oneItemInvoiceDTO.getCustomerId())
                .identityType(IdentityType.valueOf(oneItemInvoiceDTO.getCustomerIdType().toUpperCase()))
                .name(oneItemInvoiceDTO.getCustomerName())
                .contact(Contact.builder().email(oneItemInvoiceDTO.getEmail()).phone(oneItemInvoiceDTO.getMobile()).build())
            .build());
        }

        Invoice invoice = Invoice.builder()
            .client(client.get())
            .customer(customer.get())
            .subtotal(oneItemInvoiceDTO.getPrice())
            .amount(oneItemInvoiceDTO.getPrice())
            .status(InvoiceStatus.APPROVED)
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

        //Add Discount if price <> item
        if (oneItemInvoiceDTO.getPrice().compareTo(item.get().getPrice().multiply(BigDecimal.valueOf(oneItemInvoiceDTO.getQuantity()))) < 0) {
            BigDecimal discountAmount = item.get().getPrice().multiply(new BigDecimal(oneItemInvoiceDTO.getQuantity())).subtract(oneItemInvoiceDTO.getPrice());
            Discount discount = Discount.builder().iPercentage(false).type(DiscountType.ITEM).value(discountAmount).build();
            invoiceItem.setDiscount(discount);
        }

        invoice.setInvoiceItems(Arrays.asList(invoiceItem));
        invoice = invoiceRepository.save(invoice);

        // Payment
        // Payment method
        Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(oneItemInvoiceDTO.getPaymentMethod().getName().toUpperCase());

        if (paymentMethod.isPresent()) {
            String paymentMethodCode = paymentMethod.get().getCode();
            switch (paymentMethodCode) {
                case Constants.SADAD:
                    String billId = paymentService.getSadadBillAccount(invoice.getId().toString());
                    Boolean sadadResult = false;
                    try {
                        sadadResult = paymentService.sadadCall(invoice.getId().toString(), paymentService.getSadadBillAccount(billId), oneItemInvoiceDTO.getPrice());
                    } catch (IOException | JSONException e) {
                        // ToDo add new exception 500 for sadad
                        throw new TbsRunTimeException("Sadad issue", e);
                    }
                    // ToDo add new exception 500 for sadad
                    if (!sadadResult) {
                        throw new TbsRunTimeException("Sadad bill creation error");
                    }
                    oneItemInvoiceDTO.setBillNumber(billId);
                break;
                case Constants.MADA:
                case Constants.VISA:
                    log.debug("CC payment method");
                break;
                default:
                    log.debug("Cash payment method");

            }
        } else {
            throw new TbsRunTimeException("Unknown payment method");
        }

        return oneItemInvoiceDTO;
    }


}
