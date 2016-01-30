package de.kopis.timeclicker.pages;

import de.kopis.timeclicker.ListEntriesCsvProducerResource;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import de.kopis.timeclicker.model.TimeSumWithDate;
import de.kopis.timeclicker.utils.DurationUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ListSumPage extends TemplatePage {
    private static final long serialVersionUID = 1L;
    private DateFormat DATE_FORMAT;

    public ListSumPage(PageParameters parameters) {
        super("Daily Sums", parameters);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        setStatelessHint(true);
        setVersioned(false);

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
                final List<TimeEntry> allEntries = getApi().list(getCurrentUser());
                // calculate overall sum per day
                final Map<Long, TimeSumWithDate> perDay = new HashMap<>();
                for (TimeEntry e : allEntries) {
                    // build the key from given TimeEntry
                    final Date entryDate = e.getStart();
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime(entryDate);
                    // reset to midnight
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    // check if date is already set as key previously
                    final Long key = cal.getTimeInMillis();
                    if (!perDay.containsKey(key)) {
                        perDay.put(key, new TimeSumWithDate(cal.getTime(), 0L));
                    }
                    // add sum to existing entry
                    final TimeSumWithDate sum = perDay.get(key);
                    sum.addDuration(new TimeSum(e).getDuration());
                }

                final List<TimeSumWithDate> sortedPerDay = Arrays.asList(perDay.values().toArray(new TimeSumWithDate[0]));
                Collections.sort(sortedPerDay, new Comparator<TimeSumWithDate>() {
                    @Override
                    public int compare(TimeSumWithDate o1, TimeSumWithDate o2) {
                        // sort DESC by start date
                        return o2.getDate().compareTo(o1.getDate());
                    }
                });
                entries.setObject(sortedPerDay);
            } catch (NotAuthenticatedException e) {
                LOGGER.severe("Can not load entries for user " + getCurrentUser() + ": " + e.getMessage());
            }
        }

        final PageableListView<TimeSumWithDate> listView = new PageableListView<TimeSumWithDate>("listView", entries, 31) {
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
