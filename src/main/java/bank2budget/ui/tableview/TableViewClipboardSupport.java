package bank2budget.ui.tableview;

import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 *
 * @author joostmeulenkamp
 */
public class TableViewClipboardSupport {

    public static void copySelectionToClipboard(TableView<?> table) {
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

    public static <S> void pasteFromClipboard(TableView<S> table) {
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
                @SuppressWarnings("unchecked")
                TableColumn<S, ?> col = (TableColumn<S, ?>) pos.getTableColumn();
                int row = pos.getRow();
                commitValueToCell(table, col, row, pasteString);
            }
            table.refresh();
            return;
        }

        @SuppressWarnings("unchecked")
        TablePosition<S, ?> startPos = (TablePosition< S, ?>) posList.get(0); // upper-left starting cell

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

                TableColumn<S, ?> col = table.getVisibleLeafColumn(tableColIndex);

                commitValueToCell(table, col, tableRow, value);
                colClipboard++;
            }
            rowClipboard++;
        }

        table.refresh();
    }

    @SuppressWarnings("unchecked")
    private static <S> void commitValueToCell(TableView<S> table, TableColumn<S, ?> col, int row, String value) {
        Object rowItem = table.getItems().get(row);

        try {
            // Try to use the onEditCommit handler first
            if (col.getOnEditCommit() != null) {
                TableColumn.CellEditEvent<S, Object> editEvent
                        = new TableColumn.CellEditEvent<>(
                                table,
                                new TablePosition<>(table, row, (TableColumn<S, Object>) col),
                                TableColumn.editCommitEvent(),
                                value
                        );
                ((EventHandler<TableColumn.CellEditEvent<S, ?>>) col.getOnEditCommit()).handle(editEvent);
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
