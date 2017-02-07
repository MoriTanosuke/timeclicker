package de.kopis.timeclicker.pages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import com.googlecode.wickedcharts.highcharts.options.Axis;
import com.googlecode.wickedcharts.highcharts.options.ChartOptions;
import com.googlecode.wickedcharts.highcharts.options.Options;
import com.googlecode.wickedcharts.highcharts.options.SeriesType;
import com.googlecode.wickedcharts.highcharts.options.Title;
import com.googlecode.wickedcharts.highcharts.options.series.SimpleSeries;
import com.googlecode.wickedcharts.wicket6.highcharts.Chart;
import de.kopis.timeclicker.ListEntriesCsvProducerResource;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSumWithDate;
import de.kopis.timeclicker.utils.DurationUtils;
import de.kopis.timeclicker.utils.TimeSumUtility;
import de.kopis.timeclicker.utils.WorkdayCalculator;

public class WeeklyListSumPage extends SecuredPage {
    private int pageSize = 12;
    private SimpleDateFormat DATE_FORMAT;

    public WeeklyListSumPage(PageParameters parameters) {
        super("Weekly Sums", parameters);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        if (getPageParameters().get("pageSize") != null) {
            final StringValue ps = getPageParameters().get("pageSize");
            pageSize = ps.toInt(12);
        }

        add(new ResourceLink("csvLink", new ListEntriesCsvProducerResource()));
        add(new Link("addEntryLink") {
            @Override
            public void onClick() {
                setResponsePage(TimeEntryPage.class);
            }
        });

        // use the locale to figure out the dateformat
        DATE_FORMAT = new SimpleDateFormat("'KW'w yyyy", getLocale());

        final ListModel<TimeSumWithDate> entries = new ListModel<TimeSumWithDate>(new ArrayList<TimeSumWithDate>());
        if (getCurrentUser() != null) {
            try {
                final List<TimeEntry> allEntries = getApi().list(pageSize * 62, 0, getCurrentUser());
                final List<TimeSumWithDate> sortedPerWeek = new TimeSumUtility().calculateWeeklyTimeSum(allEntries);

                entries.setObject(sortedPerWeek);
            } catch (NotAuthenticatedException e) {
                getLOGGER().severe("Can not load entries for user " + getCurrentUser() + ": " + e.getMessage());
            }
        }

        final PageableListView<TimeSumWithDate> listView = new PageableListView<TimeSumWithDate>("listView", entries, pageSize) {
            @Override
            protected void populateItem(final ListItem<TimeSumWithDate> item) {
                item.add(new Label("entryDate", DATE_FORMAT.format(item.getModelObject().getDate())));
                item.add(new Label("entrySum", DurationUtils.getReadableDuration(item.getModelObject().getDuration())));

                final long dailyDuration = getDailyDuration(getCurrentUser());
                final int workingDays = WorkdayCalculator.getWorkingDaysForCurrentWeek();
                final long weeklyDuration = dailyDuration * workingDays;
                final long duration = item.getModelObject().getDuration();
                item.add(new Label("entryRemaining", DurationUtils.getReadableDuration(weeklyDuration - duration)));
            }
        };
        final PagingNavigator navigator = new PagingNavigator("paginator", listView);
        add(navigator);
        add(listView);

        final Map<String, Number> weeklySums = new HashMap<>();
        for (TimeSumWithDate entry : entries.getObject()) {
            final double durationInHours = new BigDecimal(entry.getDuration() / (60.0 * 60.0 * 1000.0)).setScale(2, RoundingMode.HALF_UP).doubleValue();
            weeklySums.put(DATE_FORMAT.format(entry.getDate()), durationInHours);
        }

        final Options chartOptions = new Options();
        chartOptions.setChartOptions(new ChartOptions().setType(SeriesType.COLUMN));
        chartOptions.setxAxis(new Axis().setCategories(Arrays.asList(weeklySums.keySet().toArray(new String[0]))));
        chartOptions.addSeries(new SimpleSeries()
                .setName("Time")
                .setData(Arrays.asList(weeklySums.values().toArray(new Number[0]))));

        chartOptions.setTitle(new Title("Weekly"));
        add(new Chart("chart", chartOptions));
    }
}
