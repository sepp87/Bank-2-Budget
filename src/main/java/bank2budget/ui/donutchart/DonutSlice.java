package bank2budget.ui.donutchart;

import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 *
 * @author joostmeulenkamp
 */
final class DonutSlice extends Path {

    void update(
            double startAngle,
            double sweepAngle,
            double innerRadius,
            double outerRadius
    ) {
        if(sweepAngle > 359) {
            drawFullDonut(innerRadius, outerRadius);
            return;
        }
        
        getElements().clear();

        double start = Math.toRadians(startAngle);
        double end = Math.toRadians(startAngle + sweepAngle);
        boolean largeArc = sweepAngle > 180;

        getElements().add(new MoveTo(
                outerRadius * Math.cos(start),
                outerRadius * Math.sin(start)
        ));

        getElements().add(new ArcTo(
                outerRadius, outerRadius,
                0,
                outerRadius * Math.cos(end),
                outerRadius * Math.sin(end),
                largeArc,
                true
        ));

        getElements().add(new LineTo(
                innerRadius * Math.cos(end),
                innerRadius * Math.sin(end)
        ));

        getElements().add(new ArcTo(
                innerRadius, innerRadius,
                0,
                innerRadius * Math.cos(start),
                innerRadius * Math.sin(start),
                largeArc,
                false
        ));

        getElements().add(new ClosePath());
    }

    private void drawFullDonut(double inner, double outer) {
        getElements().clear();

        // Outer circle (two arcs)
        getElements().add(new MoveTo(outer, 0));
        getElements().add(new ArcTo(outer, outer, 0, -outer, 0, true, true));
        getElements().add(new ArcTo(outer, outer, 0, outer, 0, true, true));

        // Inner circle (reverse direction)
        getElements().add(new MoveTo(inner, 0));
        getElements().add(new ArcTo(inner, inner, 0, -inner, 0, true, false));
        getElements().add(new ArcTo(inner, inner, 0, inner, 0, true, false));

        getElements().add(new ClosePath());
    }
}
