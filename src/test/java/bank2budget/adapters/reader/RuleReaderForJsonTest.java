package bank2budget.adapters.reader;

import bank2budget.core.Rule;
import bank2budget.adapters.reader.RuleReaderForJson;
import bank2budget.core.CashTransaction;
import bank2budget.core.Rule;
import bank2budget.core.RuleTest;
import bank2budget.core.UtilTest;
import java.text.ParseException;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author joost
 */
public class RuleReaderForJsonTest {

    @BeforeAll
    @AfterAll
    public static void printLine() {
        System.out.println();
    }


    /**
     * Test of readFrom method, of class RuleReader.
     */
    @Test
    public void testReadFrom_WhenStringContainsThreeRules_ThenReturnThreeRules() {
        System.out.println("testReadFrom_WhenStringContainsThreeRules_ThenReturnThreeRules");

        // Perform test
        RuleReaderForJson reader = new RuleReaderForJson(null);
        List<Rule> rules = reader.readFrom(RuleTest.THREE_RULES);

        // Prepare results
        int expected = 3;
        int result = rules.size();

        // Evaluate result
        assertEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }

}
