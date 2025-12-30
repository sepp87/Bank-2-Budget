package bank2budget.ui.tableview;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 *
 * @author joostmeulenkamp
 */
public class CellFactoryUtil {

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> choiceCellFactory(ObservableList<T> values) {
        return col -> new ChoiceBoxTableCell<>(values) {
        };
    }

    public static <S> Callback<TableColumn<S, Void>, TableCell<S, Void>> removeButtonCellFactory() {
        return col -> new TableCell<>() {
            private final Button button = new Button("✕");

            {
                button.getStyleClass().add("remove-row-button");

                button.setOnAction(e -> {
                    S item = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(button);
                }
            }
        };
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> editableTextCellFactory() {
        return col -> new TextFieldTableCell<>(new DefaultStringConverter()) {

            @Override
            public void startEdit() {
                super.startEdit();

                if (getGraphic() instanceof TextField editor) {
                    editor.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                        if (e.getCode() == KeyCode.TAB) {
                            commitEdit(editor.getText());
                        }
                    });
                }
            }
        };
    }

    public static <S> Callback<TableColumn<S, BigDecimal>, TableCell<S, BigDecimal>> editableAmountCellFactory(StringConverter<BigDecimal> converter) {
        return col -> new TextFieldTableCell<>(converter) {

            @Override
            public void startEdit() {
                super.startEdit();

                if (getGraphic() instanceof TextField editor) {
                    editor.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                        if (e.getCode() == KeyCode.TAB) {
                            commitEdit(getConverter().fromString(editor.getText()));
                        }
                    });
                }
            }

            @Override
            public void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(value.setScale(2, RoundingMode.HALF_UP).toPlainString());
                }

                setAlignment(Pos.CENTER_RIGHT);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();

                BigDecimal value = getItem();
                if (value != null) {
                    setText(value.setScale(2, RoundingMode.HALF_UP).toPlainString());
                } else {
                    setText(null);
                }
            }
        };
    }
}
