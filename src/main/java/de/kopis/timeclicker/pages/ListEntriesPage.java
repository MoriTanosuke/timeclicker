package de.kopis.timeclicker.pages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import de.kopis.timeclicker.ListEntriesCsvProducerResource;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
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

public class ListEntriesPage extends SecuredPage {
    private static final long serialVersionUID = 1L;
    private DateFormat DATE_FORMAT;

    private int pageSize = 31;
    private IModel<Integer> pageSizeModel = new PropertyModel<>(this, "pageSize");
    ;
    private IModel<Long> entriesCountModel;
    private DataView<TimeEntry> listView;

    public ListEntriesPage(PageParameters parameters) {
        super("Time Entries", parameters);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        if (getPageParameters().get("pageSize") != null) {
            final StringValue ps = getPageParameters().get("pageSize");
            pageSize = ps.toInt(31);
        }

        entriesCountModel = new LoadableDetachableModel<Long>() {
            @Override
            protected Long load() {
                long count = 0L;
                try {
                    count = (long) getApi().countAvailableEntries(getCurrentUser());
                } catch (NotAuthenticatedException e) {
                    getLOGGER().severe("Can not count entries for user " + getCurrentUser() + ": " + e.getMessage());
                }
                return count;
            }
        };

        // use the locale to figure out the dateformat
        DATE_FORMAT = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy Z", getLocale());
        try {
            final TimeZone tz = getTimeZone(getCurrentUser());
            DATE_FORMAT.setTimeZone(tz);
        } catch (NotAuthenticatedException e) {
            getLOGGER().fine("Can not load timezone for user " + getCurrentUser() + ": " + e.getMessage());
        }

        add(new ResourceLink("csvLink", new ListEntriesCsvProducerResource(pageSizeModel.getObject())));
        add(new Link("addEntryLink") {
            @Override
            public void onClick() {
                setResponsePage(TimeEntryPage.class);
            }
        });

        final DropDownChoice<Integer> pageSizeInput = new DropDownChoice<Integer>("pageSizeInput", pageSizeModel, Arrays.asList(7, 14, 31, 90, 180)) {
            @Override
            protected void onSelectionChanged(final Integer newSelection) {
                super.onSelectionChanged(newSelection);
                //TODO update datatable?
            }
        };
        pageSizeInput.setVisible(false);
        add(pageSizeInput);

        final IDataProvider<TimeEntry> entries = new IDataProvider<TimeEntry>() {
            final List<TimeEntry> entriesOnPage = new ArrayList<>();

            @Override
            public void detach() {
                // TODO do nothing or clear entriesOnPage?
            }

            @Override
            public Iterator iterator(final long first, final long count) {
                entriesOnPage.clear();
                getLOGGER().finer("Showing " + count + " entries for page " + (first / pageSizeModel.getObject()) + ", first=" + first + " pageSize=" + pageSizeModel.getObject());
                try {
                    entriesOnPage.addAll(getApi().list((int) count, (int) (first / pageSizeModel.getObject()), getCurrentUser()));
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
            public IModel<TimeEntry> model(final TimeEntry object) {
                return Model.of(object);
            }
        };
        listView = new DataView<TimeEntry>("listView", entries) {
            @Override
            public long getPageCount() {
                long pageCount = entriesCountModel.getObject() / pageSizeModel.getObject();
                return pageCount + 1;
            }

            @Override
            public long getItemsPerPage() {
                return pageSizeModel.getObject();
            }

            @Override
            protected void populateItem(final Item<TimeEntry> item) {
                item.add(new Label("entryKey", item.getModelObject().getKey()));
                item.add(new Label("entryStart", DATE_FORMAT.format(item.getModelObject().getStart())));
                final TimeSum timeSum = new TimeSum(item.getModelObject());
                if (item.getModelObject().getStop() != null) {
                    item.add(new Label("entryStop", DATE_FORMAT.format(item.getModelObject().getStop())));
                    item.add(new Label("entrySum", Model.of(timeSum.getReadableDuration())));
                } else {
                    item.add(new Label("entryStop", "-"));
                    item.add(new Label("entrySum", "-"));
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
                            // remove from API
                            getApi().delete(item.getModelObject().getKey(), getCurrentUser());
                            // TODO update datatable
                        } catch (NotAuthenticatedException e) {
                            getLOGGER().severe("Can not delete entry " + item.getModelObject().getKey());
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
