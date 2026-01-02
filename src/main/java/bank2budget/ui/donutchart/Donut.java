package bank2budget.ui.donutchart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

/**
 *
 * @author joostmeulenkamp
 */
public final class Donut extends Region {

    private final double holeRatio;
    private List<DonutData> data = List.of();
    private final List<DonutSlice> slices = new ArrayList<>();

    public Donut(double holeRatio) {

        this.holeRatio = holeRatio;

        getStyleClass().add("donut-chart");

    }

    public void setData(List<DonutData> data) {
        this.data = data;
        slices.clear();
        getChildren().clear();

        int i = 0;
        for (DonutData d : data) {
            DonutSlice slice = new DonutSlice();

            slice.setStyle("-slice-fill: " + DonutChart.getColor(i) + ";");

            slice.getStyleClass().add("donut-slice");
            slice.getStyleClass().add("donut-slice-" + ++i);
            slice.getStyleClass().add("donut-slice-" + d.name().toLowerCase());

            Tooltip.install(slice, new Tooltip(d.name()));

            slices.add(slice);
            getChildren().add(slice);
        }

        requestLayout();
    }

    @Override
    protected void layoutChildren() {
        if (slices.isEmpty()) {
            return;
        }

        double size = Math.min(getWidth(), getHeight());
        if (size <= 0) {
            return;
        }

        double cx = getWidth() / 2;
        double cy = getHeight() / 2;

        double outer = size / 2;
        double inner = outer * holeRatio;

        double total = data.stream().mapToDouble(DonutData::value).sum();
        double angle = -90;

        for (int i = 0; i < slices.size(); i++) {
            DonutSlice slice = slices.get(i);
            DonutData d = data.get(i);

            double sweep = d.value() / total * 360;

            slice.update(angle, sweep, inner, outer);
            slice.setTranslateX(cx);
            slice.setTranslateY(cy);

            angle += sweep;
        }
    }
}
