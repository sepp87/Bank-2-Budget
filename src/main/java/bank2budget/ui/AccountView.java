package bank2budget.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountView extends TableView<EditableCashTransaction> {

    private final ObservableList<String> categorySuggestions = FXCollections.observableArrayList();

    public AccountView() {
        this.setEditable(true);
        this.getSelectionModel().setCellSelectionEnabled(true);
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<EditableCashTransaction, Integer> transactionNumberColumn = buildColumn("Transaction Number", EditableCashTransaction::transactionNumber);
        TableColumn<EditableCashTransaction, String> descriptionColumn = buildColumn("Description", EditableCashTransaction::description);
        TableColumn<EditableCashTransaction, String> notesColumn = buildEditableColumn("Notes", EditableCashTransaction::notes, EditableCashTransaction::setNotes);
        TableColumn<EditableCashTransaction, String> contraAccountNameColumn = buildColumn("Contra Account Name", EditableCashTransaction::contraAccountName);
        
        descriptionColumn.setPrefWidth(240);
        notesColumn.setPrefWidth(240);
        contraAccountNameColumn.setPrefWidth(240);
        
        this.getColumns().add(buildAutoCompleteColumn("Category", EditableCashTransaction::category, EditableCashTransaction::setCategory, categorySuggestions));
        this.getColumns().add(buildColumn("Amount", EditableCashTransaction::amount));
        this.getColumns().add(transactionNumberColumn);
        this.getColumns().add(buildColumn("Date", EditableCashTransaction::date));
        this.getColumns().add(buildColumn("Account Balance", EditableCashTransaction::accountBalance));
        this.getColumns().add(buildColumn("Account Name", EditableCashTransaction::accountName));
        this.getColumns().add(contraAccountNameColumn);
        this.getColumns().add(descriptionColumn);
        this.getColumns().add(notesColumn);

        transactionNumberColumn.setSortType(TableColumn.SortType.DESCENDING);
        this.getSortOrder().add(transactionNumberColumn);
        this.sort();

    }

    public void load(List<EditableCashTransaction> transactions) {
        loadCategorySuggestions(transactions);
        this.getItems().clear();
        this.getItems().addAll(transactions);
        this.sort();

    }

    private void loadCategorySuggestions(List<EditableCashTransaction> transactions) {
        categorySuggestions.clear();
        Set<String> availableCategories = new HashSet<>();
        for (EditableCashTransaction t : transactions) {
            availableCategories.add(t.category());
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

  

}
