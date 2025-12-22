package bank2budget.ui;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountBalanceView extends VBox {

    private final PieChart pie;

    private final Map<String, PieChart.Data> data = new TreeMap<>();

    public AccountBalanceView() {

        this.pie = new PieChart();
        
        pie.setLabelsVisible(false);
        pie.setLegendVisible(true);
        pie.setStartAngle(90);

        getChildren().addAll(pie);

    }

    public void setData(Map<String, BigDecimal> balances) {
        HashSet<String> toRemove = new HashSet<>(data.keySet());
        toRemove.removeAll(balances.keySet());
        
        for(var key : toRemove) {
            var removed = data.remove(key);
            pie.getData().remove(removed);
        }

        for (var entry : balances.entrySet()) {
            if (data.containsKey(entry.getKey())) {
                data.get(entry.getKey()).setPieValue(entry.getValue().doubleValue());
            } else {
                var newData = new PieChart.Data(entry.getKey(), entry.getValue().doubleValue());
                data.put(entry.getKey(), newData);
                pie.getData().add(newData);
            }
        }
    }
}
