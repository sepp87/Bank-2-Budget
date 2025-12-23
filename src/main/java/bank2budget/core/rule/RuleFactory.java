package bank2budget.core.rule;

import bank2budget.core.CashTransaction;
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

    private final static Map<String, Function<CashTransaction, String>> GETTERS = Map.of(
            "description", CashTransaction::description,
            "contraAccountName", CashTransaction::contraAccountName
    );

    private final static Map<String, BiFunction<CashTransaction, String, CashTransaction>> SETTERS = Map.of(
            "category", CashTransaction::withCategory
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
            if(config.resultField().equals("category") && transaction.category() != null) {
                return false;
            }
            String actualValue = getter.apply(transaction);
            return actualValue != null && actualValue.toLowerCase().contains(config.checkValue().toLowerCase());
        };
    }

    private static Function<CashTransaction, CashTransaction> createResult(RuleConfig config) {
        BiFunction<CashTransaction, String, CashTransaction> setter = SETTERS.get(config.resultField());

        return transaction -> {
            return setter.apply(transaction, config.resultValue());
        };
    }

    public static List<Rule<CashTransaction>> createSystemRules(Map<String, String> myAccounts) {
        return List.of(
                createInternalRule(myAccounts),
                createOverwriteAccountNameRule(myAccounts),
                createOverwriteContraAccountNameRule(myAccounts)
        );
    }

    private static Rule<CashTransaction> createInternalRule(Map<String, String> myAccounts) {

        Predicate<CashTransaction> check = tx -> {
            String number = tx.contraAccountNumber();
            return number != null && myAccounts.containsKey(number) && tx.internal() != true;
        };

        Function<CashTransaction, CashTransaction> result = tx -> {
            return tx.withInternal(true);
        };

        return new Rule<>(check, result);
    }

    private static Rule<CashTransaction> createOverwriteAccountNameRule(Map<String, String> myAccounts) {

        Predicate<CashTransaction> check = tx -> {
            String number = tx.accountNumber();
            boolean isMyAccount = number != null && myAccounts.containsKey(number);
            if (isMyAccount) {
                String name = myAccounts.get(number);
                return !name.equals(tx.accountName());
            }
            return isMyAccount;
        };

        Function<CashTransaction, CashTransaction> result = tx -> {
            return tx.withAccountName(myAccounts.get(tx.accountNumber()));
        };

        return new Rule<>(check, result);
    }

    private static Rule<CashTransaction> createOverwriteContraAccountNameRule(Map<String, String> myAccounts) {

        Predicate<CashTransaction> check = tx -> {
            String number = tx.contraAccountNumber();
            boolean isMyAccount = number != null && myAccounts.containsKey(number);
            if (isMyAccount) {
                String name = myAccounts.get(number);
                return !name.equals(tx.contraAccountName());
            }
            return isMyAccount;
        };

        Function<CashTransaction, CashTransaction> result = tx -> {
            return tx.withContraAccountName(myAccounts.get(tx.contraAccountNumber()));
        };

        return new Rule<>(check, result);
    }

}
