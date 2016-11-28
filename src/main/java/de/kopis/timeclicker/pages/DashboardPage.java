package de.kopis.timeclicker.pages;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class DashboardPage extends SecuredPage {
    public DashboardPage(PageParameters parameters) {
        super("Dashboard", parameters);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        final String highcharts = "$(function () {\n" +
                "            $.getJSON('/startchart.json', callback);\n" +
                "});\n\n" +
                "function round(number, precision) {\n" +
                "    var factor = Math.pow(10, precision);\n" +
                "    var tempNumber = number * factor;\n" +
                "    var roundedTempNumber = Math.round(tempNumber);\n" +
                "    return roundedTempNumber / factor;\n" +
                "};" +
                "function callback(data) {\n" +
                "           Highcharts.chart('container', {\n" +
                "                    chart: {\n" +
                "                        type: 'arearange',\n" +
                "                        zoomType: 'x'\n" +
                "                    },\n" +
                "                    title: {\n" +
                "                        text: 'Work'\n" +
                "                    },\n" +
                "                    xAxis: {\n" +
                "                        type: 'datetime'\n," +
                "                        tickInterval: 24 * 3600 * 1000,\n" +
                "                    },\n" +
                "                    yAxis: {\n" +
                "                        type: 'time'\n," +
                "                        title: 'Working Hours'\n" +
                "                    },\n" +
                "                    legend: {\n" +
                "                        enabled: false\n" +
                "                    },\n" +
                "                    series: [{\n" +
                "                        name: 'Duration',\n" +
                "                        data: data\n" +
                "                    }]\n" +
                "\n" +
                "         });\n" +
                "};\n";
        response.render(JavaScriptContentHeaderItem.forScript(highcharts, "highcharts"));
    }
}
