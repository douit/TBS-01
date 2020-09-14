package sa.tamkeentech.tbs.service;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.User;

import io.github.jhipster.config.JHipsterProperties;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import sa.tamkeentech.tbs.service.dto.FileDTO;

/**
 * Service for sending emails.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";

    private static final String BASE_URL = "baseUrl";

    private static final String LINK = "link";

    private static final String USER_NAME = "userName";

    private final JHipsterProperties jHipsterProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;

    @Inject
    private MailProperties mailProperties;

    @Inject
    @Lazy
    private ReportService reportService;

    public MailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender,
            MessageSource messageSource, SpringTemplateEngine templateEngine) {

        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml, FileDTO attachment) {
        log.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            if (attachment != null) {
                message.addAttachment(attachment.getName(),  new ByteArrayResource(attachment.getBytes()));
            }

            // --- Debug Code
            log.debug("----- spring: mail: -----");
            log.debug("spring mail: host={}", mailProperties.getHost());
            log.debug("spring mail: port={}", mailProperties.getPort());
            log.debug("spring mail: username={}", mailProperties.getUsername());
            // log.debug("spring mail: password={}", mailProperties.getPassword());
            log.debug("spring mail: protocol={}", mailProperties.getProtocol());

            log.debug("----- spring: mail: properties.mail.smtp -----");
            for (Map.Entry<String, String> entry : mailProperties.getProperties().entrySet()) {
                log.debug("properties.mail.smtp: {}={}", entry.getKey(), entry.getValue());
            }

            log.debug("----- session object -----");
            for (Map.Entry<Object, Object> entry : message.getMimeMessage().getSession().getProperties().entrySet()) {
                log.debug("session: {}={}", entry.getKey(), entry.getValue());
            }
            // --- Debug Code

            javaMailSender.send(mimeMessage);
            log.debug("Sent email to User '{}'", to);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Email could not be sent to user '{}'", to, e);
            } else {
                log.warn("Email could not be sent to user '{}': {}", to, e.getMessage());
            }
        }
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        if (user.isInternal() != true) {
            context.setVariable(LINK, "#/account/reset/finish?key=" + user.getResetKey());
        } else {
            context.setVariable(LINK, "#/");
        }

        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmail(user.getEmail(), subject, content, false, true, null);
    }

    @Async
    public void sendActivationEmail(User user) {
        log.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/activationEmail", "email.activation.title");
    }

    @Async
    public void sendCreationEmail(User user) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/creationEmail", "email.activation.title");
    }

    @Async
    public void sendPasswordResetMail(User user) {
        log.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/passwordResetEmail", "email.reset.title");
    }

    @Async
    public void sendReceiptMailWithAttachment(String email, Long invoiceId, String receiverName, String clientCode) {
        log.debug("Sending Receipt email to '{}'", email);
        FileDTO attachment = reportService.generateInvoiceReceipt(invoiceId);
        log.debug("Sending Receipt email to '{}'", email);
        Locale locale = Locale.forLanguageTag(Constants.LANGUAGE.ARABIC.getLanguageKey());
        Context context = new Context(locale);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        context.setVariable(USER_NAME, receiverName);
        String content = templateEngine.process("mail/receiptEmail", context);
        String subject = messageSource.getMessage("email.receipt.title", null, locale);
        sendClientMail(email, subject, content, true, true, attachment, clientCode);
    }


    private void sendClientMail(String to, String subject, String content, boolean isMultipart, boolean isHtml, FileDTO attachment, String clientCode) {
        Properties props = new Properties();
        // props.put("mail.smtp.host", "10.102.11.18");
        props.put("mail.smtp.host", "10.60.73.8");
        Session session;
        String from;
        if ("MNAR".equals(clientCode)) {
             session = Session.getDefaultInstance(props, null);
            from = "info@mnar.sa";
        } else {
             session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailProperties.getUsername(), mailProperties.getPassword());
                }
            });
            from = "billing@tamkeentech.sa";
        }


        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart();
            // Set text message part
            MimeBodyPart messageBodyPart1 = new MimeBodyPart();
            // messageBodyPart1.setText(content);
            messageBodyPart1.setContent( content, "text/html; charset=utf-8" );
            // Set Attachment
            MimeBodyPart messageBodyPart2 = new MimeBodyPart();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // Now use your ByteArrayDataSource as
            DataSource source = new ByteArrayDataSource(attachment.getBytes(), "application/pdf");
            messageBodyPart2.setDataHandler(new DataHandler(source));
            messageBodyPart2.setFileName(attachment.getName());

            multipart.addBodyPart(messageBodyPart1);
            multipart.addBodyPart(messageBodyPart2);

            // Send the complete message parts
            message.setContent(multipart);
            message.saveChanges();
            // Set the Date: header
            message.setSentDate(new java.util.Date());

            log.debug("Sending email ....");
            Thread.currentThread().setContextClassLoader( MailService.class.getClassLoader());
            Transport.send(message);
            log.debug("Sent Email Successfully");

        } catch (MessagingException e) {
            log.debug("Sent Email issue : " + e.getMessage());
            e.printStackTrace();
        }catch (Exception e) {
            log.debug("Sent Email issue 2 : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
