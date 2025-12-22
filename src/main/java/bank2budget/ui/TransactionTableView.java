package bank2budget.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionTableView extends TableView<EditableCashTransaction> {

    private final ObservableList<String> categorySuggestions = FXCollections.observableArrayList();

    public TransactionTableView() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        this.setEditable(true);
        this.getSelectionModel().setCellSelectionEnabled(true);
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        this.addEventHandler(TableColumn.editCommitEvent(), e -> this.requestFocus());
        this.addEventHandler(TableColumn.editCancelEvent(), e -> this.requestFocus());

        TableColumn<EditableCashTransaction, Integer> transactionNumberColumn = TableViewUtil.buildColumn("Transaction Number", EditableCashTransaction::transactionNumber);
        TableColumn<EditableCashTransaction, String> descriptionColumn = TableViewUtil.buildColumn("Description", EditableCashTransaction::description);
        TableColumn<EditableCashTransaction, String> notesColumn = TableViewUtil.buildEditableTextColumn("Notes", EditableCashTransaction::notes, EditableCashTransaction::setNotes);
        TableColumn<EditableCashTransaction, String> contraAccountNameColumn = TableViewUtil.buildColumn("Contra Account Name", EditableCashTransaction::contraAccountName);

        descriptionColumn.setPrefWidth(240);
        notesColumn.setPrefWidth(240);
        contraAccountNameColumn.setPrefWidth(240);

        this.getColumns().add(TableViewUtil.buildAutoCompleteColumn("Category", EditableCashTransaction::category, EditableCashTransaction::setCategory, categorySuggestions));
        this.getColumns().add(TableViewUtil.buildColumn("Amount", EditableCashTransaction::amount));
        this.getColumns().add(transactionNumberColumn);
        this.getColumns().add(TableViewUtil.buildColumn("Date", EditableCashTransaction::date));
        this.getColumns().add(TableViewUtil.buildColumn("Account Balance", EditableCashTransaction::accountBalance));
        this.getColumns().add(TableViewUtil.buildColumn("Account Name", EditableCashTransaction::accountName));
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
}
