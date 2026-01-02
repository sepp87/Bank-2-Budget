package bank2budget.ui.dashboard;

import bank2budget.ui.donutchart.DonutData;
import bank2budget.ui.donutchart.DonutChart;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountBalanceView extends VBox {

    private final DonutChart chart;

    public AccountBalanceView() {
        this.chart = new DonutChart();

        getChildren().addAll(chart);
    }

    public void setData(Map<String, BigDecimal> balances) {
        var data = balances.entrySet().stream().map(e -> new DonutData(e.getKey(), e.getValue().doubleValue())).toList();
        chart.setData(data);
    }

}
