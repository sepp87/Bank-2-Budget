package bank2budget.ui;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.V;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountController {

    private final AccountView view;
    private final ObservableList<EditableCashTransaction> transactions;

    public AccountController(AccountView transactionsView, ObservableList<EditableCashTransaction> transactions) {
        this.view = transactionsView;
        this.transactions = transactions;

        view.setOnKeyPressed((e) -> {
            handleShortcutTriggered(e, view);
        });
        
        
        view.load(transactions);
    }
    
    public void reload(ObservableList<EditableCashTransaction> transactions) {
        this.transactions.clear();
        this.transactions.addAll(transactions);
    }
    
    public ObservableList<EditableCashTransaction> transactions() {
        return transactions;
    }

    public static void handleShortcutTriggered(KeyEvent event, TableView<EditableCashTransaction> transactionsView) {

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

    private static void pasteFromClipboard(TableView<EditableCashTransaction> table) {
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
                TableColumn<EditableCashTransaction, ?> col = (TableColumn<EditableCashTransaction, ?>) pos.getTableColumn();
                int row = pos.getRow();
                commitValueToCell(table, col, row, pasteString);
            }
            table.refresh();
            return;
        }

        TablePosition<EditableCashTransaction, ?> startPos = posList.get(0); // upper-left starting cell

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

                TableColumn<EditableCashTransaction, ?> col = table.getVisibleLeafColumn(tableColIndex);

                commitValueToCell(table, col, tableRow, value);
                colClipboard++;
            }
            rowClipboard++;
        }

        table.refresh();
    }

    @SuppressWarnings("unchecked")
    private static <S> void commitValueToCell(TableView<EditableCashTransaction> table, TableColumn<EditableCashTransaction, ?> col, int row, String value) {
        Object rowItem = table.getItems().get(row);

        try {
            // Try to use the onEditCommit handler first
            if (col.getOnEditCommit() != null) {
                TableColumn.CellEditEvent<EditableCashTransaction, Object> editEvent
                        = new TableColumn.CellEditEvent<>(
                                table,
                                new TablePosition<>(table, row, (TableColumn<EditableCashTransaction, Object>) col),
                                TableColumn.editCommitEvent(),
                                value
                        );
                ((EventHandler<TableColumn.CellEditEvent<EditableCashTransaction, ?>>) col.getOnEditCommit()).handle(editEvent);
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
