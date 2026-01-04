package bank2budget.ui.tableview;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;

/**
 *
 * @author joostmeulenkamp
 */
public class TableColumnUtil {

    public static <S> TableColumn<S, Void> buildRemoveButtonColumn() {
        TableColumn<S, Void> column = new TableColumn<>("");
        column.setCellFactory(CellFactoryUtil.removeButtonCellFactory());
        return column;
    }

    public static <S> TableColumn<S, BigDecimal> buildAmountColumn(String title, Function<S, BigDecimal> getter) {
        return buildColumn(title, getter, amountCellFactory());
    }

    public static <S, T> TableColumn<S, T> buildColumn(String title, Function<S, T> getter) {
        return buildColumn(title, getter, null);
    }

    private static <S, T> TableColumn<S, T> buildColumn(String title, Function<S, T> getter, Callback<TableColumn<S, T>, TableCell<S, T>> cellFactory) {
        TableColumn<S, T> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(getter.apply(data.getValue())));
        if (cellFactory != null) {
            col.setCellFactory(cellFactory);

        }
        return col;
    }

    private static <S> Callback<TableColumn<S, BigDecimal>, TableCell<S, BigDecimal>> amountCellFactory() {

        return col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(value.setScale(2, RoundingMode.HALF_UP).toPlainString());
                }

                setAlignment(Pos.CENTER_RIGHT);
            }
        };
    }

    public static <S> TableColumn<S, String> buildEditableTextColumn(String title, Function<S, String> getter, BiConsumer<S, String> setter, Runnable afterEdit) {
//        return buildEditableColumn(title, getter, setter, TextFieldTableCell.forTableColumn(), afterEdit);
        return buildEditableColumn(title, getter, setter, CellFactoryUtil.editableTextCellFactory(), afterEdit);

    }

    public static <S> TableColumn<S, BigDecimal> buildEditableAmountColumn(String title, Function<S, BigDecimal> getter, BiConsumer<S, BigDecimal> setter, Runnable afterEdit) {
        var converter = StringConverterUtil.bigDecimalConverter();
        return buildEditableColumn(title, getter, setter, CellFactoryUtil.editableAmountCellFactory(converter), afterEdit);
    }

    public static <S, T> TableColumn<S, T> buildEditableChoiceColumn(String title, Function<S, T> getter, BiConsumer<S, T> setter, ObservableList<T> values, Runnable afterEdit) {
        Callback<TableColumn<S, T>, TableCell<S, T>> choiceCellFactory = tc -> new ChoiceBoxTableCell<>(values);
        var column = buildEditableColumn(title, getter, setter, choiceCellFactory, afterEdit);
        column.getProperties().put("EDIT_ON_SELECT", true);
        return column;
    }

    private static <S, T> TableColumn<S, T> buildEditableColumn(String title, Function<S, T> getter, BiConsumer<S, T> setter, Callback<TableColumn<S, T>, TableCell<S, T>> cellFactory, Runnable afterEdit) {
        TableColumn<S, T> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(getter.apply(data.getValue())));
        col.setCellFactory(cellFactory);
        col.setOnEditCommit(e -> {
            setter.accept(e.getRowValue(), e.getNewValue());
            if (afterEdit != null) {
                afterEdit.run();
            }
        });
        col.setOnEditCancel(e -> {
            if (afterEdit != null) {
                afterEdit.run();
            }
        });
        col.setEditable(true);
        return col;
    }

    public static <S> TableColumn<S, String> buildAutoCompleteColumn(
            String title,
            Function<S, String> getter,
            BiConsumer<S, String> setter,
            ObservableList<String> suggestions,
            Runnable afterEdit
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

                    // Attach ControlsFX autocompletion - WARNING: BROKEN since string s is NULL
//                    var binding = TextFields.bindAutoCompletion(
//                            editor,
//                            request -> suggestions.filtered(s -> s.toLowerCase().contains(request.getUserText().toLowerCase())
//                            ));
//
//                    editor.getProperties().put("autocomplete", binding);

                }
            }
        });

        // Commit edited value back into the model
        col.setOnEditCommit(e -> {
            setter.accept(e.getRowValue(), e.getNewValue());
            if (afterEdit != null) {
                afterEdit.run();
            }
        });
        col.setOnEditCancel(e -> {
            if (afterEdit != null) {
                afterEdit.run();
            }
        });

        return col;
    }

}
