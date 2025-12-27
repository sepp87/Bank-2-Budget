package bank2budget.ui.transaction;

import bank2budget.ui.tableview.TableConfigurator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionTableView extends TableView<EditableCashTransaction> {

    private final ObservableList<String> categorySuggestions = FXCollections.observableArrayList();

    public TransactionTableView() {

        var configurator = new TableConfigurator<>(this);

        configurator.addEditableTextColumWithAutocomplete("Category", EditableCashTransaction::category, EditableCashTransaction::setCategory, categorySuggestions);
        configurator.addAmountColumn("Amount", EditableCashTransaction::amount);
        var transactionNumberColumn = configurator.addColumn("Transaction Number", EditableCashTransaction::transactionNumber);
        configurator.addColumn("Date", EditableCashTransaction::date);
        configurator.addAmountColumn("Account Balance", EditableCashTransaction::accountBalance);
        configurator.addColumn("Account Name", EditableCashTransaction::accountName);
        var contraAccountNameColumn = configurator.addColumn("Contra Account Name", EditableCashTransaction::contraAccountName);
        var descriptionColumn = configurator.addColumn("Description", EditableCashTransaction::description);
        var notesColumn = configurator.addEditableTextColumn("Notes", EditableCashTransaction::notes, EditableCashTransaction::setNotes);

        descriptionColumn.setPrefWidth(240);
        notesColumn.setPrefWidth(240);
        contraAccountNameColumn.setPrefWidth(240);

        transactionNumberColumn.setSortType(TableColumn.SortType.DESCENDING);
        this.getSortOrder().add(transactionNumberColumn);
        this.sort();

    }

    public void load(List<EditableCashTransaction> transactions) {
        loadCategorySuggestions(transactions);
        this.getItems().setAll(transactions);
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
