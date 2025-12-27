package bank2budget.ui.tableview;

import java.math.BigDecimal;
import javafx.util.StringConverter;

/**
 *
 * @author joostmeulenkamp
 */
public class StringConverterUtil {

    public static StringConverter<BigDecimal> bigDecimalConverter() {
        return new StringConverter<BigDecimal>() {

            private BigDecimal previous;

            @Override
            public String toString(BigDecimal value) {
                previous = value;
                return value != null ? value.toPlainString() : "";
            }

            @Override
            public BigDecimal fromString(String string) {
                if (string == null) {
                    return previous;
                }

                string = string.trim().replace(",", ".");

                if (string.matches("^[+-]?[0-9]+(\\.[0-9]+)?$")) {
                    return new BigDecimal(string);
                }

                return previous;
            }
        };
    }
}
