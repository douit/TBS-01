package sa.tamkeentech.tbs.service.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.incrementer.PostgresSequenceMaxValueIncrementer;
import org.springframework.stereotype.Component;
import sa.tamkeentech.tbs.config.Constants;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

@Component
public class SequenceUtil {

    @Inject
    DataSource dataSource;

    PostgresSequenceMaxValueIncrementer sequenceMaxValueIncrementer = new PostgresSequenceMaxValueIncrementer();

    final List<String> PREDEFINED_SEQ = Arrays.asList(Constants.CLIENT_SADAD_CONFIG.MUSANED.name(),
        Constants.CLIENT_SADAD_CONFIG.AJIR.name(), Constants.CLIENT_SADAD_CONFIG.TAHAQAQ.name());

/*    @Autowired
    DataSource dataSource;*/

    public Long getNextInvoiceNumber(String clientCode) {
        if (PREDEFINED_SEQ.contains(clientCode)) {
            sequenceMaxValueIncrementer.setDataSource(dataSource);
            sequenceMaxValueIncrementer.setIncrementerName(String.format(Constants.INVOICE_DEFAULT_SEQ, clientCode.toLowerCase()));
            return sequenceMaxValueIncrementer.nextLongValue();
        }
        return null;
    }


}
