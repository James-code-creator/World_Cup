package worldcupbraket.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import worldcupbraket.domain.Player;

import java.awt.image.BufferedImage;
import java.util.List;

public class GraphService {
    public static BufferedImage createPlayersScoreBoardGraph(List<Player> players) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        players.forEach(player -> {
            List<Integer> points = player.getPointProgression();
            XYSeries series = new XYSeries(player.name);
            for (int i = 0; i < points.size(); i++) {
                series.add(i, points.get(i));
            }
            dataset.addSeries(series);
        });
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Points Per Match",
                "Match Number",
                "Points",
                dataset
        );
        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer =
                (XYLineAndShapeRenderer) plot.getRenderer();

        renderer.setDefaultItemLabelsVisible(true);

        renderer.setDefaultItemLabelGenerator((set, series, item) -> {
            int last = set.getItemCount(series) - 1;

            if (item == last) {
                return set.getSeriesKey(series).toString();
            }

            return null;
        });

        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(
                        ItemLabelAnchor.OUTSIDE2,
                        TextAnchor.CENTER_LEFT
                )
        );
        chart.setPadding(new RectangleInsets(10, 10, 10, 30));
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setUpperMargin(0.2);
        return chart.createBufferedImage(800, 800);
    }
}