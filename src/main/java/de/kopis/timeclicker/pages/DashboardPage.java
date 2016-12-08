package de.kopis.timeclicker.pages;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

public class DashboardPage extends SecuredPage {
    private int pageSize = 31;
    private int page = 0;

    public DashboardPage(PageParameters parameters) {
        super("Dashboard", parameters);

        if (parameters.get("page") != null) {
            final StringValue ps = parameters.get("page");
            page = ps.toInt(page);
        }
        if (parameters.get("pageSize") != null) {
            final StringValue ps = parameters.get("pageSize");
            pageSize = ps.toInt(pageSize);
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        final String highcharts = "$(function () {\n" +
                "            $.getJSON('/startchart.json?page=" + page + "&pageSize=" + pageSize + "', callback);\n" +
                "});\n\n" +
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
                "                        title: 'Working Hours'\n," +
                "                    },\n" +
                "                    tooltip: {\n" +
                "                        formatter: function () {\n" +
                "                             var hours1 = Math.floor(this.point.low);\n" +
                "                             var minutes1 = ('000' + Math.floor((this.point.low - hours1) * 60)).slice(-2);\n" +
                "                             var hours2 = Math.floor(this.point.high);\n" +
                "                             var minutes2 = ('000' + Math.floor((this.point.high - hours2) * 60)).slice(-2);\n" +
                "                             return hours1 + ':' + minutes1 + ' - ' + hours2 + ':' + minutes2;\n" +
                "                        }\n" +
                "                    }," +
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
