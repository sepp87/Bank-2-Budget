package bank2budget.ui;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetView extends AnchorPane {

    private final ComboBox<LocalDate> monthSelection;

    private final Label currentMonth;
    private final Label remainingIncome;
    private final Label currentBalance;
    private final Label lastExport;

    private final ProfitAndLossView profitAndLossView;
    private final BudgetedVsActualView budgetedVsActualView;

    // Month Year                                               [ month ] [ save ]
    // Budgeted vs. Expenses                                    Savings, Buffers and Deficits
    // Header   category - budgeted - expenses - remainder      Header  category - amount
    // Row      category - budgeted - expenses - remainder      Row     category - amount
//    public BudgetView(App app) {
    public BudgetView() {

        // Build header
        this.currentMonth = new Label("January 2026");
        this.lastExport = new Label("2025-01-01");
        this.remainingIncome = new Label("€500,-");
        this.currentBalance = new Label("€1000,-");
        GridPane header = new GridPane();
        header.getStyleClass().add("grid-row");
        ColumnConstraints width25Percent = new ColumnConstraints();
        width25Percent.setPercentWidth(25);
        header.getColumnConstraints().addAll(width25Percent, width25Percent, width25Percent, width25Percent);
        header.addRow(0,
                wrapKpiWithLabel(currentMonth, "Selected month"),
                wrapKpiWithLabel(lastExport, "Last export date"),
                wrapKpiWithLabel(remainingIncome, "Available budget"),
                wrapKpiWithLabel(currentBalance, "Closing balance")
        );

        // Build tables
        Label budgetedVsActualHeader = new Label("Budgeted vs. Actual");
        budgetedVsActualHeader.getStyleClass().add("header");
//        budgetedVsActualHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        budgetedVsActualView = new BudgetedVsActualView();
        VBox budgetedVsExpenses = new VBox(budgetedVsActualHeader, budgetedVsActualView);

        Label savingsBuffersAndDeficitsHeader = new Label("Savings, Buffers and Deficits");
        savingsBuffersAndDeficitsHeader.getStyleClass().add("header");
//        savingsBuffersAndDeficitsHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        this.profitAndLossView = new ProfitAndLossView();
        VBox savingsBuffersAndDeficits = new VBox(savingsBuffersAndDeficitsHeader, profitAndLossView);

        ColumnConstraints width37_5Percent = new ColumnConstraints();
        width37_5Percent.setPercentWidth(37.5);
        ColumnConstraints categoryConstraint = new ColumnConstraints();
        categoryConstraint.setPrefWidth(200);
        ColumnConstraints numberConstraint = new ColumnConstraints();
        numberConstraint.setPrefWidth(80);

        GridPane tables = new GridPane();
        tables.getStyleClass().add("grid-row");
        tables.getColumnConstraints().addAll(width37_5Percent, width37_5Percent, width25Percent);
        tables.addRow(0,
                wrapInStackPane(budgetedVsExpenses),
                wrapInStackPane(savingsBuffersAndDeficits)
        );

        // Build menu
        this.monthSelection = new ComboBox<>();
        Button save = new Button("Save");
        HBox menu = new HBox(3, monthSelection, save);
        AnchorPane.setRightAnchor(menu, 10.);
        AnchorPane.setBottomAnchor(menu, 10.);

        VBox budgetOverview = new VBox(header, tables);
        budgetOverview.getStyleClass().add("grid-root");
        budgetOverview.setFillWidth(true);

        AnchorPane.setLeftAnchor(budgetOverview, 0.0);
        AnchorPane.setRightAnchor(budgetOverview, 0.0);

        this.getChildren().addAll(budgetOverview, menu);

    }

    public ProfitAndLossView getProfitAndLossView() {
        return profitAndLossView;
    }

    public BudgetedVsActualView getBudgetedVsActualView() {
        return budgetedVsActualView;
    }

    public Label lastExport() {
        return lastExport;
    }

    public Label selectedMonth() {
        return currentMonth;
    }

    public void selectedMonth(int month, int year) {
        String monthYear = capitalize(Month.of(month).toString()) + " " + year;
        currentMonth.setText(monthYear);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase()
                + s.substring(1).toLowerCase();
    }

    public Label variance() {
        return remainingIncome;
    }

    public Label closing() {
        return currentBalance;
    }

    public ComboBox<LocalDate> monthSelector() {
        return monthSelection;
    }

    public void populateMonthSelector(Collection<LocalDate> months, LocalDate selected) {
        TreeSet<LocalDate> monthsDescending = new TreeSet<>(Comparator.reverseOrder());
        months.forEach(monthsDescending::add);

        for (LocalDate month : monthsDescending) {
            monthSelection.getItems().add(month);
        }
        if (selected == null || !months.contains(selected)) {
            monthSelection.getSelectionModel().selectFirst();
        } else {
            monthSelection.getSelectionModel().select(selected);
        }
    }

    private StackPane wrapKpiWithLabel(Node node, String title) {
        node.getStyleClass().add("kpi");
        VBox box = new VBox();
        Label label = new Label(title);
        label.getStyleClass().add("kpi-label");
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(node, label);
        return wrapInStackPane(box);
    }

    private StackPane wrapInStackPane(Node node) {
        StackPane pane = new StackPane(node);
        pane.getStyleClass().add("tile");
        return pane;
    }

}
