package bank2budget.core.rule;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author joostmeulenkamp
 */
public class Rule<T> {

    private final Predicate<T> check;
    private final Function<T, T> result;

    public Rule(Predicate<T> check, Function<T, T> result) {
        this.check = check;
        this.result = result;
    }

    public T apply(T t) {
        if (check.test(t)) {
            result.apply(t);
        }
        return t;
    }
}
