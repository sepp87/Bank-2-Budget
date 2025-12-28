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

    public static StringConverter<Integer> integerConverter() {
        return new StringConverter<Integer>() {
            private Integer previous;

            @Override
            public String toString(Integer value) {
                previous = value;
                return value == null ? "1" : value.toString();
            }

            @Override
            public Integer fromString(String value) {
                if (value == null) {
                    return previous;
                }

                value = value.trim();

                if (value.matches("^[+-]?\\d+$")) {
                    return Integer.valueOf(value);
                }

                return previous;
            }
        };
    }
}
