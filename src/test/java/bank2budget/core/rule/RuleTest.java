package bank2budget.core.rule;

import bank2budget.core.CashTransactionBuilder;
import bank2budget.core.UtilTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author joostmeulenkamp
 */
public class RuleTest {

    @org.junit.jupiter.api.BeforeAll
    @org.junit.jupiter.api.AfterAll
    public static void printLine() {
        System.out.println();
    }

    @Test
    public void test_WhenDescriptionFoo_ThenCategoryBar() {
        System.out.println("test_WhenDescriptionFoo_ThenCategoryBar");

        // create test data
        var transaction = new CashTransactionBuilder().description("Foo").build();
        var rule = RuleFactory.create(new RuleConfig("description", "Foo", "category", "Bar"));

        // perform test
        var processed = rule.apply(transaction);

        // prepare results
        String expected = "Bar";
        String result = processed.category();

        // evaluate result
        Assertions.assertEquals(expected, result);
        UtilTest.printResult(expected, result);

    }

    @Test
    public void test_WhenContraAccountNameFoo_ThenCategoryBar() {
        System.out.println("test_WhenContraAccountNameFoo_ThenCategoryBar");

        // create test data
        var transaction = new CashTransactionBuilder().description("Foo").build();
        var rule = RuleFactory.create(new RuleConfig("description", "Foo", "category", "Bar"));

        // perform test
        var processed = rule.apply(transaction);

        // prepare results
        String expected = "Bar";
        String result = processed.category();

        // evaluate result
        Assertions.assertEquals(expected, result);
        UtilTest.printResult(expected, result);

    }
}
