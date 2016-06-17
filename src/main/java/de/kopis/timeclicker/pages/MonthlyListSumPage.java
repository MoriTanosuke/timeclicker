package de.kopis.timeclicker.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.kopis.timeclicker.ListEntriesCsvProducerResource;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSumWithDate;
import de.kopis.timeclicker.utils.DurationUtils;
import de.kopis.timeclicker.utils.TimeSumUtility;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

public class MonthlyListSumPage extends TemplatePage {
    private int pageSize = 12;
    private SimpleDateFormat DATE_FORMAT;

    public MonthlyListSumPage(PageParameters parameters) {
        super("Monthly Sums", parameters);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        if (getPageParameters().get("pageSize") != null) {
            final StringValue ps = getPageParameters().get("pageSize");
            pageSize = ps.toInt(31);
        }

        add(new ResourceLink("csvLink", new ListEntriesCsvProducerResource()));
        add(new Link("addEntryLink") {
            @Override
            public void onClick() {
                setResponsePage(TimeEntryPage.class);
            }
        });

        // use the locale to figure out the dateformat
        DATE_FORMAT = new SimpleDateFormat("MM.yyyy", getLocale());

        final ListModel<TimeSumWithDate> entries = new ListModel<TimeSumWithDate>(new ArrayList<TimeSumWithDate>());
        if (getCurrentUser() != null) {
            try {
                final List<TimeEntry> allEntries = getApi().list(pageSize, getCurrentUser());
                final List<TimeSumWithDate> sortedPerMonth = new TimeSumUtility().calculateMonthlyTimeSum(allEntries);

                entries.setObject(sortedPerMonth);
            } catch (NotAuthenticatedException e) {
                LOGGER.severe("Can not load entries for user " + getCurrentUser() + ": " + e.getMessage());
            }
        }

        final PageableListView<TimeSumWithDate> listView = new PageableListView<TimeSumWithDate>("listView", entries, pageSize) {
            @Override
            protected void populateItem(final ListItem<TimeSumWithDate> item) {
                item.add(new Label("entryDate", DATE_FORMAT.format(item.getModelObject().getDate())));
                item.add(new Label("entrySum", DurationUtils.getReadableDuration(item.getModelObject().getDuration())));
            }
        };
        final PagingNavigator navigator = new PagingNavigator("paginator", listView);
        add(navigator);
        add(listView);
    }
}
