package bank2budget.ui.donutchart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 *
 * @author joostmeulenkamp
 */
public class DonutChart extends Region {

    private final Donut donut;
    private List<DonutData> data = List.of();
    private final VBox legend;

    public DonutChart() {

        this.donut = new Donut(0.7);

        var wrapper = new StackPane(donut);
        wrapper.prefWidthProperty().bind(this.widthProperty());
        wrapper.prefHeightProperty().bind(this.widthProperty());
        this.legend = new VBox(5);
        var root = new VBox(20, wrapper, legend);

        getChildren().addAll(root);
    }

    public void setData(List<DonutData> data) {
        this.data = normalizeData(data);
        donut.setData(this.data);
        updateLegend();
    }

    private static final double FACTOR = 0.008; // values smaller than 0.8% are visually enlarged for readability, if there are more than one it is shown as one mixed category.

    private List<DonutData> normalizeData(List<DonutData> data) {
        var sorted = data.stream().sorted(Comparator.comparingDouble(DonutData::value).reversed()).toList();
        double total = sorted.stream().mapToDouble(DonutData::value).sum();
        double threshold = total * FACTOR;

        List<DonutData> above = sorted.stream().filter(e -> e.value() >= threshold).toList();
        List<DonutData> below = sorted.stream().filter(e -> e.value() < threshold).toList();

        List<DonutData> result = new ArrayList<>();

        if (below.size() > 1) {
            double totalOfOther = below.stream().mapToDouble(DonutData::value).sum();
            result.addAll(above);
            result.add(new DonutData("Other", totalOfOther));

        } else {
            result.addAll(data);
        }

        return result;
    }

    private void updateLegend() {
        legend.getChildren().clear();
        double total = data.stream().mapToDouble(DonutData::value).sum();

        int i = 0;
        for (var item : data) {
            Circle circle = new Circle(10);
            circle.setStyle("-slice-fill: " + DonutChart.getColor(i) + ";");
            circle.getStyleClass().add("donut-slice");
            circle.getStyleClass().add("donut-slice-" + ++i);
            circle.getStyleClass().add("donut-slice-" + item.name().toLowerCase());

            var percentage = String.format(Locale.US, "%.1f", item.value() / total * 100) + "%";
            Label label = new Label(item.name() + " (" + percentage + " / € " + item.value() + ")");
            HBox entry = new HBox(10, circle, label);
            entry.prefWidthProperty().bind(this.widthProperty());
            legend.getChildren().add(entry);
        }
    }

    public static String getColor(int i) {
        return COLORS[i % COLORS.length];
    }

    private static final String[] COLORS = new String[]{
        "#4C6EF5", // indigo blue
        "#40C057", // fresh green
        "#FF922B", // bright orange
        "#845EF7", // violet
        "#FFD43B", // warm yellow

        "#339AF0", // sky blue
        "#20C997", // teal green
        "#E64980", // rose pink
        "#5F3DC4", // deep violet
        "#A9E34B", // fresh lime

        "#22B8CF", // cyan
        "#F03E3E", // clean red
        "#69DB7C", // mint green
        "#CC5DE8", // orchid
        "#FCC419", // golden yellow

        "#1864AB", // deep blue
        "#FF6B6B", // coral red
        "#12B886", // emerald teal
        "#9775FA", // soft purple
        "#FAB005", // amber

        "#4DABF7", // bright azure
        "#0CA678", // strong teal
        "#FF8787", // light coral
        "#BE4BDB", // magenta
        "#C0EB75", // pastel lime

        "#66D9E8", // ice blue
        "#A61E4D", // wine pink
        "#63E6BE", // soft turquoise
        "#7048E8", // royal purple
        "#EAD000", // sunflower

        "#3BC9DB", // aqua
        "#FA5252", // soft red
        "#96F2D7", // light aqua
        "#DA77F2", // lavender
        "#94D82D", // electric lime

        "#5C7CFA", // royal blue
        "#087F5B", // forest teal
        "#FF8CC3", // neon rose
        "#D6336C", // raspberry
        "#ADB5BD", // cool gray

        "#495057", // graphite
        "#CED4DA", // light gray
        "#868E96", // slate gray
        "#343A40" // dark UI anchor
    };

}
