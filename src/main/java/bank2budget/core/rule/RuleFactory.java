package bank2budget.core.rule;

import bank2budget.core.Transaction;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class RuleFactory {

    private final static Logger LOGGER = Logger.getLogger(RuleFactory.class.getName());

    private final static Map<String, Function<Transaction, String>> GETTERS = Map.of(
            "description", Transaction::description,
            "contraAccountName", Transaction::contraAccountName
    );

    private final static Map<String, BiFunction<Transaction, String, Transaction>> SETTERS = Map.of(
            "category", Transaction::withCategory
    );

    public static Rule<Transaction> create(RuleConfig config) {
        Rule<Transaction> r = new Rule<>(
                createCheck(config),
                createResult(config)
        );

        return r;
    }

    private static Predicate<Transaction> createCheck(RuleConfig config) {
        Function<Transaction, String> getter = GETTERS.get(config.checkField());

        return transaction -> {
            String actualValue = getter.apply(transaction);
            return actualValue != null && actualValue.contains(config.checkValue());
        };
    }

    private static Function<Transaction, Transaction> createResult(RuleConfig config) {
        BiFunction<Transaction, String, Transaction> setter = SETTERS.get(config.resultField());

        return transaction -> {
            return setter.apply(transaction, config.resultValue());
        };
    }

    public static List<Rule<Transaction>> createSystemRules(Map<String, String> myAccounts) {
        return List.of(
                createInternalRule(myAccounts),
                createOverwriteAccountNameRule(myAccounts),
                createOverwriteContraAccountNameRule(myAccounts)
        );
    }

    private static Rule<Transaction> createInternalRule(Map<String, String> myAccounts) {

        Predicate<Transaction> check = tx -> {
            String number = tx.contraAccountNumber();
            return number != null && myAccounts.containsKey(number) && tx.internal() != true;
        };

        Function<Transaction, Transaction> result = tx -> {
            return tx.withInternal(true);
        };

        return new Rule<>(check, result);
    }

    private static Rule<Transaction> createOverwriteAccountNameRule(Map<String, String> myAccounts) {

        Predicate<Transaction> check = tx -> {
            String number = tx.accountNumber();
            boolean isMyAccount = number != null && myAccounts.containsKey(number);
            if (isMyAccount) {
                String name = myAccounts.get(number);
                return !name.equals(tx.accountName());
            }
            return isMyAccount;
        };

        Function<Transaction, Transaction> result = tx -> {
            return tx.withAccountName(myAccounts.get(tx.accountNumber()));
        };

        return new Rule<>(check, result);
    }

    private static Rule<Transaction> createOverwriteContraAccountNameRule(Map<String, String> myAccounts) {

        Predicate<Transaction> check = tx -> {
            String number = tx.contraAccountNumber();
            boolean isMyAccount = number != null && myAccounts.containsKey(number);
            if (isMyAccount) {
                String name = myAccounts.get(number);
                return !name.equals(tx.contraAccountName());
            }
            return isMyAccount;
        };

        Function<Transaction, Transaction> result = tx -> {
            return tx.withContraAccountName(myAccounts.get(tx.contraAccountNumber()));
        };

        return new Rule<>(check, result);
    }

}
