package bank2budget.adapters.reader;

import bank2budget.cli.CliAppRunner;
import bank2budget.core.CashTransaction;
import bank2budget.core.rule.Rule;
import bank2budget.core.rule.RuleConfig;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class RuleFactory {

    private final static Logger LOGGER = Logger.getLogger(CliAppRunner.class.getName());

    private final static Map<String, Function<CashTransaction, String>> GETTERS = Map.of("description", CashTransaction::description,
            "contraAccountName", CashTransaction::contraAccountName
    );

    private final static Map<String, BiConsumer<CashTransaction, String>> SETTERS = Map.of(
            "category", CashTransaction::setCategory
    );

    public static Rule<CashTransaction> create(RuleConfig config) {
        Rule<CashTransaction> r = new Rule<>(
                createCheck(config),
                createResult(config)
        );

        return r;
    }

    private static Predicate<CashTransaction> createCheck(RuleConfig config) {
        Function<CashTransaction, String> getter = GETTERS.get(config.checkField());

        return transaction -> {
            String actualValue = getter.apply(transaction);
            return actualValue != null && actualValue.contains(config.checkValue());
        };
    }

    private static Function<CashTransaction, CashTransaction> createResult(RuleConfig config) {
        BiConsumer<CashTransaction, String> setter = SETTERS.get(config.resultField());

        return transaction -> {
            setter.accept(transaction, config.resultValue());
            return transaction;
        };
    }

    private static Rule<CashTransaction> createInternalRule(Map<String, String> myAccounts) {

        Predicate<CashTransaction> check = tx -> {
            String number = tx.contraAccountNumber();
            return number != null && myAccounts.containsKey(number);
        };

        Function<CashTransaction, CashTransaction> result = tx -> {
            tx.setInternal(true);
            return tx;
        };

        return new Rule<>(check, result);
    }

    private static Rule<CashTransaction> createOverwriteAccountNameRule(Map<String, String> myAccounts) {

        Predicate<CashTransaction> check = tx -> {
            String number = tx.accountNumber();
            return number != null && myAccounts.containsKey(number);
        };

        Function<CashTransaction, CashTransaction> result = tx -> {
            tx.setAccountName(myAccounts.get(tx.accountNumber()));
            return tx;
        };

        return new Rule<>(check, result);
    }

    private static Rule<CashTransaction> createOverwriteContraAccountNameRule(Map<String, String> myAccounts) {

        Predicate<CashTransaction> check = tx -> {
            String number = tx.contraAccountNumber();
            return number != null && myAccounts.containsKey(number);
        };

        Function<CashTransaction, CashTransaction> result = tx -> {
            tx.setContraAccountName(myAccounts.get(tx.contraAccountNumber()));
            return tx;
        };

        return new Rule<>(check, result);
    }

}
