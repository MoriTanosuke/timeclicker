package de.kopis.timeclicker.pages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.kopis.timeclicker.ListEntriesCsvProducerResource;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSumWithDate;
import de.kopis.timeclicker.model.UserSettings;
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

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.users.User;

public class ListSumPage extends TemplatePage {
    private static final long serialVersionUID = 1L;
    private DateFormat DATE_FORMAT;
    private int pageSize = 31;

    public ListSumPage(PageParameters parameters) {
        super("Daily Sums", parameters);
    }

    @Override
    public void onInitialize() {
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
        DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy Z", getLocale());

        final ListModel<TimeSumWithDate> entries = new ListModel<TimeSumWithDate>(new ArrayList<TimeSumWithDate>());
        if (getCurrentUser() != null) {
            try {
                final List<TimeEntry> allEntries = getApi().list(pageSize, getCurrentUser());
                final List<TimeSumWithDate> sortedPerDay = new TimeSumUtility().calculateDailyTimeSum(allEntries);

                entries.setObject(sortedPerDay);
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
                final long duration = item.getModelObject().getDuration();
                item.add(new Label("entryRemaining", DurationUtils.getReadableDuration(dailyDuration - duration)));
            }
        };
        final PagingNavigator navigator = new PagingNavigator("paginator", listView);
        add(navigator);
        add(listView);
    }


    private long getDailyDuration(final User user) {
        long dailyDuration = 0L;
        try {
            final UserSettings settings = getApi().getUserSettings(null, user);
            dailyDuration = settings.getWorkingDurationPerDay();
        } catch (NotAuthenticatedException | EntityNotFoundException e) {
            getLOGGER().warning("Can not load settings for user " + user + ".");
        }
        return dailyDuration;
    }
}
