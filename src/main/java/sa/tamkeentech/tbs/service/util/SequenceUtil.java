package sa.tamkeentech.tbs.service.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.incrementer.PostgresSequenceMaxValueIncrementer;
import org.springframework.stereotype.Component;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.repository.ClientRepository;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

@Component
public class SequenceUtil {

    @Inject
    DataSource dataSource;

    @Inject
    ClientRepository clientRepository;

    PostgresSequenceMaxValueIncrementer sequenceMaxValueIncrementer = new PostgresSequenceMaxValueIncrementer();

    /*final List<String> PREDEFINED_SEQ = Arrays.asList(Constants.CLIENT_SADAD_CONFIG.MUSANED.name(),
        Constants.CLIENT_SADAD_CONFIG.AJIR.name(), Constants.CLIENT_SADAD_CONFIG.TAHAQAQ.name());*/

/*    @Autowired
    DataSource dataSource;*/

    public Long getNextInvoiceNumber(String clientCode) {

        if (StringUtils.isNotEmpty(clientCode)) {
            try {
                sequenceMaxValueIncrementer.setDataSource(dataSource);
                sequenceMaxValueIncrementer.setIncrementerName(String.format(Constants.INVOICE_DEFAULT_SEQ, clientCode.toLowerCase()));
                return sequenceMaxValueIncrementer.nextLongValue();
            } catch (DataAccessException e) {
                throw new TbsRunTimeException("Unable to get invoice number");
            }
        }
        return null;
    }


}
