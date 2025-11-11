package bank2budget.core;

import bank2budget.core.Rule;
import bank2budget.adapters.reader.RuleReaderForJson;
import bank2budget.core.CashTransaction;
import bank2budget.core.CashTransactionTest;
import java.text.ParseException;
import java.time.LocalDate;
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
public class RuleTest {

    @BeforeAll
    @AfterAll
    public static void printLine() {
        System.out.println();
    }

    public static final String THREE_RULES = """
                                             [
                                                {
                                                    "if": {
                                                        "description": "Supermarket"
                                                    },
                                                    "operator": {
                                                        "description": "=="
                                                    },
                                                    "then": {
                                                        "category": "Groceries"                           
                                                    }
                                                },
                                                {
                                                    "if": {
                                                        "amount": "-1000"
                                                    },
                                                    "operator": {
                                                        "amount": "<"
                                                    },
                                                    "then": {
                                                        "category": "Expensive stuff"                           
                                                    }
                                                },
                                                {
                                                    "if": {
                                                        "amount": "100"
                                                    },
                                                    "operator": {
                                                        "amount": ">"
                                                    },
                                                    "then": {
                                                        "category": "Woohoo"                           
                                                    }
                                                }
                                             ]
                                             """;

    /**
     * Test of process method, of class Rule.
     */
    @Test
    public void testProcess_WhenDescriptionContainsString_ThenSetCategory() {
        System.out.println("testProcess_WhenDescriptionContainsString_ThenSetCategory");

        // Create test data
        RuleReaderForJson reader = new RuleReaderForJson(null);
        Rule supermarketRule = reader.readFrom(THREE_RULES).get(0);
        CashTransaction transaction = CashTransactionTest.generateOneTransaction("abc", LocalDate.now(), null, null);
        transaction.setDescription("Best supermarket ever");

        // Perform test
        supermarketRule.process(transaction);

        // Prepare results
        String expected = "Groceries";
        String result = transaction.getCategory();

        // Evaluate result
        assertEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }

    /**
     * Test of process method, of class Rule.
     */
    @Test
    public void testProcess_WhenAmountLessThanMinusOneThousand_ThenSetCategoryExpensiveStuff() {
        System.out.println("testProcess_WhenAmountLessThanMinusOneThousand_ThenSetCategoryExpensiveStuff");

        // Create test data
        RuleReaderForJson reader = new RuleReaderForJson(null);
        Rule expensiveStuffRule = reader.readFrom(THREE_RULES).get(1);
        CashTransaction transaction = CashTransactionTest.generateOneTransaction("abc", LocalDate.now(), null, null);
        transaction.setAmount(-2000);

        // Perform test
        expensiveStuffRule.process(transaction);

        // Prepare results
        String expected = "Expensive stuff";
        String result = transaction.getCategory();

        // Evaluate result
        assertEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }

    /**
     * Test of process method, of class Rule.
     */
    @Test
    public void testProcess_WhenAmountGreaterThanOneHundred_ThenSetCategoryWoohoo() {
        System.out.println("testProcess_WhenAmountGreaterThanOneHundred_ThenSetCategoryWoohoo");

        // Create test data
        RuleReaderForJson reader = new RuleReaderForJson(null);
        Rule woohooRule = reader.readFrom(THREE_RULES).get(2);
        CashTransaction transaction = CashTransactionTest.generateOneTransaction("abc", LocalDate.now(), null, null);
        transaction.setAmount(200);

        // Perform test
        woohooRule.process(transaction);

        // Prepare results
        String expected = "Woohoo";
        String result = transaction.getCategory();

        // Evaluate result
        assertEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }
}
