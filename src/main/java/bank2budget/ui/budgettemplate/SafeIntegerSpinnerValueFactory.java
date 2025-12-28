package bank2budget.ui.budgettemplate;

import javafx.scene.control.SpinnerValueFactory;

/**
 *
 * @author joostmeulenkamp
 */
public final class SafeIntegerSpinnerValueFactory
        extends SpinnerValueFactory.IntegerSpinnerValueFactory {

    private int fallback;

    public SafeIntegerSpinnerValueFactory(int min, int max, int initial) {
        super(min, max, initial);
        this.fallback = initial;

        valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fallback = newVal;
            }
        });
    }

    @Override
    public void increment(int steps) {
        if (getValue() == null) {
            setValue(fallback);
        }
        super.increment(steps);
    }

    @Override
    public void decrement(int steps) {
        if (getValue() == null) {
            setValue(fallback);
        }
        super.decrement(steps);
    }
}
