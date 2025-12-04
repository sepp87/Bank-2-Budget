package bank2budget.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class DateUtil {
    
    
    public static List<LocalDate> dateRange(LocalDate from, LocalDate to) {
        List<LocalDate> result =  new ArrayList<>();
        if(from.isEqual(to)) {
            result.add(from);
            return result;
        }
        while(from.isBefore(to)) {
            result.add(from);
            from = from.plusDays(1);
        }
        result.add(to);
        return result;
    }
}
