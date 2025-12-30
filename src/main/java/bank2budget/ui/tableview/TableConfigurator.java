package bank2budget.ui.tableview;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author joostmeulenkamp
 */
public class TableConfigurator<S> {

    private final TableView<S> table;
    private final EventHandler<KeyEvent> shortcutHandler;

    public TableConfigurator(TableView<S> table) {
        this.table = table;

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        table.setOnKeyTyped((e) -> TableViewEditSupport.onKeyTyped(e, table));
        TableViewEditSupport.enableEditOnSelect(table);
        this.shortcutHandler = (e) -> TableViewShortcutSupport.shortcutPressed(e, table);
        table.addEventFilter(KeyEvent.KEY_PRESSED, shortcutHandler);
    }

    public <T> TableColumn<S, T> addColumn(String title, Function<S, T> getter) {
        var column = TableColumnUtil.buildColumn(title, getter);
        table.getColumns().add(column);
        return column;
    }

    public TableColumn<S, BigDecimal> addAmountColumn(String title, Function<S, BigDecimal> getter) {
        var column = TableColumnUtil.buildAmountColumn(title, getter);
        table.getColumns().add(column);
        return column;
    }

    public TableColumn<S, String> addEditableTextColumn(String title, Function<S, String> getter, BiConsumer<S, String> setter) {
        var column = TableColumnUtil.buildEditableTextColumn(title, getter, setter, table::requestFocus);
        table.getColumns().add(column);
        return column;
    }

    public TableColumn<S, String> addEditableTextColumWithAutocomplete(String title, Function<S, String> getter, BiConsumer<S, String> setter, ObservableList<String> values) {
        var column = TableColumnUtil.buildAutoCompleteColumn(title, getter, setter, values, table::requestFocus);
        table.getColumns().add(column);
        return column;
    }

    public TableColumn<S, BigDecimal> addEditableAmountColumn(String title, Function<S, BigDecimal> getter, BiConsumer<S, BigDecimal> setter) {
        var column = TableColumnUtil.buildEditableAmountColumn(title, getter, setter, table::requestFocus);
        table.getColumns().add(column);
        return column;
    }

    public <T> TableColumn<S, T> addEditableChoiceColumn(String title, Function<S, T> getter, BiConsumer<S, T> setter, ObservableList<T> values) {
        var column = TableColumnUtil.buildEditableChoiceColumn(title, getter, setter, values, table::requestFocus);
        table.getColumns().add(column);
        return column;
    }

    public TableColumn<S, Void> addRemoveButtonColumn() {
        TableColumn<S, Void> column = TableColumnUtil.buildRemoveButtonColumn();
        table.getColumns().add(column);
        return column;
    }

    public void remove() {
        table.setOnKeyTyped(null);
        table.removeEventFilter(KeyEvent.KEY_PRESSED, shortcutHandler);
    }
}
