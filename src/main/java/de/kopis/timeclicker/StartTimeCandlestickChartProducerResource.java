package de.kopis.timeclicker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.string.StringValue;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class StartTimeCandlestickChartProducerResource extends AbstractResource {
    private static final Logger LOGGER = Logger.getLogger(StartTimeCandlestickChartProducerResource.class.getName());
    private static final float TWENTY_FOUR_HOURS_IN_MILLISECONDS = 24 * 60 * 60 * 1000;

    private static final TimeclickerAPI api = new TimeclickerAPI();
    private int pageSize = 31;
    private int page = 0;

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        if (attributes.getParameters().get("page") != null) {
            final StringValue ps = attributes.getParameters().get("page");
            page = ps.toInt(page);
        }
        if (attributes.getParameters().get("pageSize") != null) {
            final StringValue ps = attributes.getParameters().get("pageSize");
            pageSize = ps.toInt(pageSize);
        }

        final ResourceResponse resourceResponse = new ResourceResponse();
        resourceResponse.setContentType("application/json");
        resourceResponse.setTextEncoding("utf-8");

        resourceResponse.setWriteCallback(new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) throws IOException {
                final UserService userService = UserServiceFactory.getUserService();
                final User currentUser = userService.getCurrentUser();

                final OutputStream outputStream = attributes.getResponse().getOutputStream();
                final Writer writer = new OutputStreamWriter(outputStream);

                writer.write("[\n");

                try {
                    final Set<DateTuple> entryByDate = reduceToDay(api.list(pageSize, page, currentUser));

                    LOGGER.info("Found " + entryByDate.size() + " entries...");
                    int count = 0;
                    for (DateTuple entry : entryByDate) {
                        // need to convert to ISO8601 for javascript
                        writer.write("[" + entry.getStart().getTime() +
                                "," + ((float) entry.getStart().getTime() % TWENTY_FOUR_HOURS_IN_MILLISECONDS / TWENTY_FOUR_HOURS_IN_MILLISECONDS) * 24.0 +
                                "," + ((float) entry.getStop().getTime() % TWENTY_FOUR_HOURS_IN_MILLISECONDS / TWENTY_FOUR_HOURS_IN_MILLISECONDS) * 24.0 +
                                "]");
                        if (++count < entryByDate.size()) {
                            writer.write(",\n");
                        }
                    }
                } catch (NotAuthenticatedException e) {
                    LOGGER.severe("Can not load entries: " + e.getMessage());
                }

                writer.write("]");

                writer.flush();
            }
        });

        return resourceResponse;
    }

    class DateTuple implements Comparable<DateTuple> {
        private Date start;
        private Date stop;

        public DateTuple(Date start, Date stop) {
            this.start = start;
            this.stop = stop;
        }

        public Date getStart() {
            return start;
        }

        public Date getStop() {
            return stop;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public void setStop(Date stop) {
            this.stop = stop;
        }

        @Override
        public int hashCode() {
            return start.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DateTuple)) return false;

            return start.equals(((DateTuple) obj).getStart());
        }

        @Override
        public int compareTo(DateTuple o) {
            return start.compareTo(o.getStart());
        }
    }

    private Set<DateTuple> reduceToDay(final List<TimeEntry> entries) {
        final Map<Long, DateTuple> perDay = new HashMap<>();

        Collections.sort(entries, new Comparator<TimeEntry>() {
            @Override
            public int compare(TimeEntry o1, TimeEntry o2) {
                // sort ASC by start date
                return o1.getStart().compareTo(o2.getStart());
            }
        });

        for (TimeEntry e : entries) {
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
                perDay.put(key, new DateTuple(e.getStart(), e.getStop()));
            }
            // set end date if later then previous end date
            final DateTuple existingEntry = perDay.get(key);
            if (e.getStop() != null && e.getStop().after(existingEntry.getStop())) {
                existingEntry.setStop(e.getStop());
            }
        }

        return new TreeSet<>(perDay.values());
    }
}
