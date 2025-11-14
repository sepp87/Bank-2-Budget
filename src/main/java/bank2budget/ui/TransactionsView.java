package bank2budget.ui;

import bank2budget.core.CashTransaction;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.V;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionsView extends TableView<CashTransaction> {

    private final ObservableList<String> categorySuggestions = FXCollections.observableArrayList();

    public TransactionsView(ObservableList<CashTransaction> transactions) {
        super(transactions);
        this.setEditable(true);
        this.getSelectionModel().setCellSelectionEnabled(true);
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<CashTransaction, Integer> transactionNumber = buildColumn("Transaction Number", CashTransaction::getTransactionNumber);

        reloadCategorySuggestions(transactions);

        this.getColumns().add(buildAutoCompleteColumn("Category", CashTransaction::getCategory, CashTransaction::setCategory, categorySuggestions));
        this.getColumns().add(buildColumn("Amount", CashTransaction::getAmount));
        this.getColumns().add(transactionNumber);
        this.getColumns().add(buildColumn("Date", CashTransaction::getDate));
        this.getColumns().add(buildColumn("Account Balance", CashTransaction::getAccountBalance));
        this.getColumns().add(buildColumn("Account Name", CashTransaction::getAccountName));
        this.getColumns().add(buildColumn("Contra Account Name", CashTransaction::getContraAccountName));
        this.getColumns().add(buildColumn("Description", CashTransaction::getDescription));
        this.getColumns().add(buildEditableColumn("Notes", CashTransaction::getNotes, CashTransaction::setNotes));

        this.setOnKeyPressed((e) -> {
            handleShortcutTriggered(e, this);
        });

        transactionNumber.setSortType(TableColumn.SortType.DESCENDING);
        this.getSortOrder().add(transactionNumber);
        this.sort();

    }

    public void reloadTransactions(List<CashTransaction> transactions) {
        reloadCategorySuggestions(transactions);
        this.getItems().clear();
        this.getItems().addAll(transactions);
        this.sort();

    }

    private void reloadCategorySuggestions(List<CashTransaction> transactions) {
        categorySuggestions.clear();
        Set<String> availableCategories = new HashSet<>();
        for (CashTransaction t : transactions) {
            availableCategories.add(t.getCategory());
        }
        categorySuggestions.addAll(availableCategories);
    }

    private <S, T> TableColumn<S, T> buildColumn(String title, Function<S, T> getter) {
        TableColumn<S, T> col = new TableColumn<>(title);
        col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>>) data
                -> new ReadOnlyObjectWrapper<>(getter.apply(data.getValue()))
        );
        return col;
    }

    private <S, T> TableColumn<S, T> buildEditableColumn(String title, Function<S, T> getter, BiConsumer<S, T> setter) {
        TableColumn<S, T> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(getter.apply(data.getValue())));
        col.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<T>() {
            @Override
            public String toString(T value) {
                return value != null ? value.toString() : "";
            }

            @Override
            public T fromString(String s) {
                return (T) s;
            }
        }));
        col.setOnEditCommit(e -> setter.accept(e.getRowValue(), e.getNewValue()));
        col.setEditable(true);
        return col;
    }

    public static <S> TableColumn<S, String> buildAutoCompleteColumn(
            String title,
            Function<S, String> getter,
            BiConsumer<S, String> setter,
            List<String> suggestions
    ) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(getter.apply(data.getValue())));
        col.setEditable(true);

        // Use a plain converter for the text field cells
        StringConverter<String> converter = new StringConverter<>() {
            @Override
            public String toString(String value) {
                return value == null ? "" : value;
            }

            @Override
            public String fromString(String s) {
                return s;
            }
        };

        // Custom cell factory to inject autocomplete binding
        col.setCellFactory(tc -> new TextFieldTableCell<S, String>(converter) {
            @Override
            public void startEdit() {
                super.startEdit();

                // When editing starts, ensure editor (TextField) is available
                if (getGraphic() instanceof TextField editor
                        && !editor.getProperties().containsKey("autocomplete")) {

                    // Attach ControlsFX autocompletion
                    TextFields.bindAutoCompletion(editor, suggestions);
                    editor.getProperties().put("autocomplete", true);
                }
            }
        });

        // Commit edited value back into the model
        col.setOnEditCommit(e -> setter.accept(e.getRowValue(), e.getNewValue()));

        return col;
    }

    public static void handleShortcutTriggered(KeyEvent event, TableView<CashTransaction> transactionsView) {

        boolean isModifierDown = isModifierDown(event);
        switch (event.getCode()) {

            case C:
                if (isModifierDown) {
                    copySelectionToClipboard(transactionsView);
                    // copy cell value
                }
                break;
            case V:
                if (isModifierDown) {
                    // paste value to selected cells
                    pasteFromClipboard(transactionsView);
                }
                break;

        }

    }

    public static boolean isModifierDown(KeyEvent event) {
        switch (determineOperatingSystem()) {
            case WINDOWS:
                return event.isControlDown();
            case MACOS:
                return event.isMetaDown();
            case LINUX:
                return event.isMetaDown();
            default:
                return event.isControlDown();
        }
    }

    public static OperatingSystem determineOperatingSystem() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("mac")) {
            return OperatingSystem.MACOS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OperatingSystem.LINUX;
        } else if (osName.contains("sunos")) {
            return OperatingSystem.SOLARIS;
        } else {
            return OperatingSystem.OTHER_OS;
        }
    }

    public enum OperatingSystem {
        WINDOWS,
        MACOS,
        LINUX,
        SOLARIS,
        OTHER_OS
    }

    private static void copySelectionToClipboard(TableView<?> table) {
        StringBuilder clipboardString = new StringBuilder();

        var selectedCells = table.getSelectionModel().getSelectedCells();

        int prevRow = -1;
        for (TablePosition<?, ?> pos : selectedCells) {
            int row = pos.getRow();
            Object cell = pos.getTableColumn().getCellData(row);

            // If this is a new row, start a new line
            if (prevRow != -1 && row != prevRow) {
                clipboardString.append('\n');
            } else if (clipboardString.length() > 0) {
                clipboardString.append('\t');
            }

            clipboardString.append(cell == null ? "" : cell.toString());
            prevRow = row;
        }

        // Place text into system clipboard
        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private static void pasteFromClipboard(TableView<CashTransaction> table) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasString()) {
            return;
        }

        String pasteString = clipboard.getString();
        if (pasteString == null || pasteString.isEmpty()) {
            return;
        }

        // Split rows by newline and columns by tab
        String[] rows = pasteString.split("\n");

        var posList = table.getSelectionModel().getSelectedCells();
        if (posList.isEmpty()) {
            return;
        }

        // ✅ Handle single-value clipboard — fill all selected cells
        if (!pasteString.contains("\t") && !pasteString.contains("\n")) {
            for (TablePosition<?, ?> pos : posList) {
                TableColumn<CashTransaction, ?> col = (TableColumn<CashTransaction, ?>) pos.getTableColumn();
                int row = pos.getRow();
                commitValueToCell(table, col, row, pasteString);
            }
            table.refresh();
            return;
        }

        TablePosition<CashTransaction, ?> startPos = posList.get(0); // upper-left starting cell

        int rowClipboard = 0;
        for (String row : rows) {
            String[] values = row.split("\t", -1); // include empty cells
            int colClipboard = 0;
            int tableRow = startPos.getRow() + rowClipboard;

            if (tableRow >= table.getItems().size()) {
                break;
            }

            for (String value : values) {
                int tableColIndex = table.getVisibleLeafIndex(startPos.getTableColumn()) + colClipboard;
                if (tableColIndex >= table.getVisibleLeafColumns().size()) {
                    break;
                }

                TableColumn<CashTransaction, ?> col = table.getVisibleLeafColumn(tableColIndex);

                commitValueToCell(table, col, tableRow, value);
                colClipboard++;
            }
            rowClipboard++;
        }

        table.refresh();
    }

    @SuppressWarnings("unchecked")
    private static <S> void commitValueToCell(TableView<CashTransaction> table, TableColumn<CashTransaction, ?> col, int row, String value) {
        Object rowItem = table.getItems().get(row);

        try {
            // Try to use the onEditCommit handler first
            if (col.getOnEditCommit() != null) {
                TableColumn.CellEditEvent<CashTransaction, Object> editEvent
                        = new TableColumn.CellEditEvent<>(
                                table,
                                new TablePosition<>(table, row, (TableColumn<CashTransaction, Object>) col),
                                TableColumn.editCommitEvent(),
                                value
                        );
                ((EventHandler<TableColumn.CellEditEvent<CashTransaction, ?>>) col.getOnEditCommit()).handle(editEvent);
            } else {
                // Optional fallback — use reflection if column header matches property name
                String property = col.getText().replace(" ", "");
                var method = rowItem.getClass().getMethod("set" + property, String.class);
                method.invoke(rowItem, value);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
