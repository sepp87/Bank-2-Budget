package io.ost.finance;

import io.ost.finance.CashTransaction;
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
public class RuleReaderTest {

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
        RuleReader reader = new RuleReader();
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
