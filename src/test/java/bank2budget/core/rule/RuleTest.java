package bank2budget.core.rule;

import bank2budget.adapters.reader.RuleFactory;
import bank2budget.core.CashTransaction;
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
        CashTransaction transaction = new CashTransaction();
        transaction.setDescription("Foo");
        var rule = RuleFactory.create(new RuleConfig("description", "Foo", "category", "Bar"));

        // perform test
        CashTransaction processed = rule.apply(transaction);

        // prepare results
        String expected = "Bar";
        String result = processed.getCategory();

        // evaluate result
        Assertions.assertEquals(expected, result);
        UtilTest.printResult(expected, result);

    }

    @Test
    public void test_WhenContraAccountNameFoo_ThenCategoryBar() {
        System.out.println("test_WhenContraAccountNameFoo_ThenCategoryBar");

        // create test data
        CashTransaction transaction = new CashTransaction();
        transaction.setDescription("Foo");
        var rule = RuleFactory.create(new RuleConfig("description", "Foo", "category", "Bar"));

        // perform test
        CashTransaction processed = rule.apply(transaction);

        // prepare results
        String expected = "Bar";
        String result = processed.getCategory();

        // evaluate result
        Assertions.assertEquals(expected, result);
        UtilTest.printResult(expected, result);

    }
}
