package de.kopis.timeclicker.utils;

import com.googlecode.wickedcharts.highcharts.options.Labels;
import com.googlecode.wickedcharts.highcharts.options.PlotBand;
import com.googlecode.wickedcharts.highcharts.options.color.HexColor;

public class ChartUtility {
    private ChartUtility() {
        // do not instantiate
    }

    public static PlotBand buildPlotBand(String hexColor, double value, String postfix) {
        return buildPlotBand(hexColor, 0, value, postfix);
    }

    public static PlotBand buildPlotBand(String hexColor, double from, double to, String postfix) {
        PlotBand plot = new PlotBand();
        plot.setColor(new HexColor(hexColor));
        plot.setFrom(from);
        plot.setTo(to);
        plot.setLabel(new Labels(to + postfix));
        return plot;
    }
}
