package de.kopis.timeclicker.pages;

import com.googlecode.wickedcharts.highcharts.options.Axis;
import com.googlecode.wickedcharts.highcharts.options.ChartOptions;
import com.googlecode.wickedcharts.highcharts.options.Options;
import com.googlecode.wickedcharts.highcharts.options.PlotBand;
import com.googlecode.wickedcharts.highcharts.options.SeriesType;
import com.googlecode.wickedcharts.highcharts.options.Title;
import com.googlecode.wickedcharts.highcharts.options.color.HexColor;
import com.googlecode.wickedcharts.highcharts.options.series.SimpleSeries;
import com.googlecode.wickedcharts.wicket6.highcharts.Chart;
import de.kopis.timeclicker.ListEntriesCsvProducerResource;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSumWithDate;
import de.kopis.timeclicker.utils.DurationUtils;
import de.kopis.timeclicker.utils.TimeSumUtility;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ListSumPage extends SecuredPage {
    private static final long serialVersionUID = 1L;
    private DateFormat DATE_FORMAT;
    private int pageSize = 31;
    private Options chartOptions;

    public ListSumPage(PageParameters parameters) {
        super("Daily Sums", parameters);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        if (getPageParameters().get("pageSize") != null) {
            final StringValue ps = getPageParameters().get("pageSize");
            pageSize = ps.toInt(pageSize);
        }

        final IModel<Long> entriesCountModel = new LoadableDetachableModel<Long>() {
            @Override
            protected Long load() {
                long count = 0L;
                try {
                    count = getApi().countAvailableDates(getCurrentUser()).count;
                } catch (NotAuthenticatedException e) {
                    getLOGGER().severe("Can not count entries for user " + getCurrentUser() + ": " + e.getMessage());
                }
                return count;
            }
        };

        add(new ResourceLink("csvLink", new ListEntriesCsvProducerResource()));
        add(new Link("addEntryLink") {
            @Override
            public void onClick() {
                setResponsePage(TimeEntryPage.class);
            }
        });

        // use the locale to figure out the dateformat
        DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy Z", getLocale());

        final Map<Date, Number> dailySums = new TreeMap<>();

        final IModel<Integer> pageSizeModel = new PropertyModel<>(this, "pageSize");
        final IDataProvider<TimeSumWithDate> entries = new IDataProvider<TimeSumWithDate>() {
            final List<TimeSumWithDate> entriesOnPage = new ArrayList<>();

            @Override
            public void detach() {
                // TODO do nothing or clear entriesOnPage?
            }

            @Override
            public Iterator iterator(final long first, final long count) {
                entriesOnPage.clear();
                dailySums.clear();
                getLOGGER().finer("Showing " + count + " entries for page " + (first / pageSizeModel.getObject()) + ", first=" + first + " pageSize=" + pageSizeModel.getObject());
                try {
                    final List<TimeEntry> allEntries = getApi().list(pageSizeModel.getObject(), 0, getCurrentUser());
                    final List<TimeSumWithDate> sortedPerDay = new TimeSumUtility().calculateDailyTimeSum(allEntries);
                    entriesOnPage.addAll(sortedPerDay);

                    for (TimeSumWithDate entry : entriesOnPage) {
                        dailySums.put(entry.getDate(), convertToHours(entry.getDuration()));
                    }

                    // clear chart
                    chartOptions.getxAxis().clear();
                    chartOptions.getSeries().clear();
                    // update chart
                    final String[] dates = new TimeSumUtility().getSortedKeys(DATE_FORMAT, dailySums);
                    chartOptions.setxAxis(new Axis().setCategories(Arrays.asList(dates)));
                    chartOptions.setyAxis(new Axis()
                            .setTitle(new Title("Hours"))
                            .addPlotBand(buildPlotBand("#efefef", convertToHours(getDailyDuration(getCurrentUser())))));
                    chartOptions.addSeries(new SimpleSeries()
                            .setName("Working Hours")
                            .setData(Arrays.asList(dailySums.values().toArray(new Number[0]))));
                } catch (NotAuthenticatedException e) {
                    getLOGGER().severe("Can not load entries for user " + getCurrentUser() + ": " + e.getMessage());
                }
                return entriesOnPage.iterator();
            }

            @Override
            public long size() {
                return entriesCountModel.getObject();
            }

            @Override
            public IModel<TimeSumWithDate> model(final TimeSumWithDate object) {
                return Model.of(object);
            }
        };

        final DataView<TimeSumWithDate> listView = new DataView<TimeSumWithDate>("listView", entries, pageSizeModel.getObject()) {
            @Override
            protected void populateItem(Item<TimeSumWithDate> item) {
                item.add(new Label("entryDate", DATE_FORMAT.format(item.getModelObject().getDate())));
                item.add(new Label("entrySum", DurationUtils.getReadableDuration(item.getModelObject().getDuration())));

                final long dailyDuration = getDailyDuration(getCurrentUser());
                final long duration = item.getModelObject().getDuration();
                item.add(new Label("entryRemaining", DurationUtils.getReadableDuration(dailyDuration - duration)));
            }
        };
        final PagingNavigator navigator = new PagingNavigator("paginator", listView);
        add(navigator);
        add(listView);

        chartOptions = new Options();
        chartOptions.setChartOptions(new ChartOptions().setType(SeriesType.COLUMN));
        final String[] dates = new TimeSumUtility().getSortedKeys(DATE_FORMAT, dailySums);
        chartOptions.setxAxis(new Axis().setCategories(Arrays.asList(dates)));
        chartOptions.addSeries(new SimpleSeries()
                .setName("Time")
                .setData(Arrays.asList(dailySums.values().toArray(new Number[0]))));

        chartOptions.setTitle(new Title("Daily"));
        add(new Chart("chart", chartOptions));
    }

    private PlotBand buildPlotBand(String hexColor, double value) {
        return getPlotBand(hexColor, 0, value);
    }

    private PlotBand getPlotBand(String hexColor, double from, double to) {
        PlotBand plot = new PlotBand();
        plot.setColor(new HexColor(hexColor));
        plot.setFrom(from);
        plot.setTo(to);
        return plot;
    }

    private double convertToHours(long workingDuration) {
        return new BigDecimal(workingDuration / (60.0 * 60.0 * 1000.0)).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

}
