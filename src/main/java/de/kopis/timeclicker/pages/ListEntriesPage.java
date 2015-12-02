package de.kopis.timeclicker.pages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.kopis.timeclicker.ListEntriesCsvProducerResource;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;

public class ListEntriesPage extends TemplatePage {
    private static final long serialVersionUID = 1L;
    private DateFormat DATE_FORMAT;

    public ListEntriesPage(PageParameters parameters) {
        super("Time Entries", parameters);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        setStatelessHint(true);
        setVersioned(false);

        add(new ResourceLink("csvLink", new ListEntriesCsvProducerResource()));
        // use the locale to figure out the dateformat
        DATE_FORMAT = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy Z", getLocale());

        final List<TimeEntry> entries = new ArrayList<>();
        if (getCurrentUser() != null) {
            try {
                entries.addAll(getApi().list(getCurrentUser()));
                Collections.sort(entries, new Comparator<TimeEntry>() {
                    @Override
                    public int compare(TimeEntry o1, TimeEntry o2) {
                        // sort DESC by start date
                        return o2.getStart().compareTo(o1.getStart());
                    }
                });
            } catch (NotAuthenticatedException e) {
                LOGGER.severe("Can not load entries for user " + getCurrentUser() + ": " + e.getMessage());
            }
        }

        final PageableListView<TimeEntry> listView = new PageableListView<TimeEntry>("listView", entries, 10) {
            @Override
            protected void populateItem(final ListItem<TimeEntry> item) {
                item.add(new Label("entryKey", item.getModelObject().getKey()));
                item.add(new Label("entryStart", DATE_FORMAT.format(item.getModelObject().getStart())));
                if (item.getModelObject().getStop() != null) {
                    item.add(new Label("entryStop", DATE_FORMAT.format(item.getModelObject().getStop())));
                } else {
                    item.add(new Label("entryStop", "-"));
                }
                if (item.getModelObject().getTags() != null) {
                    item.add(new Label("tags",item.getModelObject().getTags()));
                } else {
                    item.add(new Label("tags", "-"));
                }
                item.add(new Link("editLink") {
                    @Override
                    public void onClick() {
                        PageParameters parameters = new PageParameters();
                        parameters.add("key", item.getModelObject().getKey());
                        setResponsePage(TimeEntryPage.class, parameters);
                    }
                });
                item.add(new Link("deleteLink") {
                    @Override
                    public void onClick() {
                        try {
                            getApi().delete(item.getModelObject().getKey(), getCurrentUser());
                        } catch (NotAuthenticatedException e) {
                            LOGGER.severe("Can not delete entry " + item.getModelObject().getKey());
                        }
                        setResponsePage(findPage());
                    }
                });
            }
        };
        final PagingNavigator navigator = new PagingNavigator("paginator", listView);
        add(navigator);
		add(listView);
    }
}
