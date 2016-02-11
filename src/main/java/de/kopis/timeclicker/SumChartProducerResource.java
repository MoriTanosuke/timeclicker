package de.kopis.timeclicker;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSumWithDate;
import de.kopis.timeclicker.utils.FormattedDurationPrinter;
import de.kopis.timeclicker.utils.TimeSumUtility;
import org.apache.wicket.request.resource.AbstractResource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class SumChartProducerResource extends AbstractResource {
    private static final Logger LOGGER = Logger.getLogger(ListEntriesChartProducerResource.class.getName());

    private static final TimeclickerAPI api = new TimeclickerAPI();

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
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

                writer.write("{\"cols\":[" +
                        "{\"label\":\"Start\",\"type\":\"string\"}" +
                        ",{\"label\":\"Tracked\",\"type\":\"number\"}" +
                        ",{\"type\": \"string\", \"role\": \"tooltip\"}" +
                        "],\"rows\":[");

                try {
                    final List<TimeEntry> rawEntries = api.list(currentUser);
                    final List<TimeSumWithDate> entries = new TimeSumUtility().calculateDailyTimeSum(rawEntries);
                    // sort ascending
                    Collections.sort(entries, new Comparator<TimeSumWithDate>() {
                        @Override
                        public int compare(TimeSumWithDate o1, TimeSumWithDate o2) {
                            // sort ASC by start date
                            return o1.getDate().compareTo(o2.getDate());
                        }
                    });

                    for (int i = 0; i < entries.size(); i++) {
                        final TimeSumWithDate entry = entries.get(i);
                        // need to convert to ISO8601 for javascript
                        writer.write("{\"c\":[" +
                                "{\"v\":\"" + entry.getDate() + "\"}" +
                                ",{\"v\":" + entry.getDuration() + "}" +
                                ",{\"v\":\"" + FormattedDurationPrinter.getReadableDuration(entry.getDuration()) + "\"}" +
                                "]}");
                        // only write "," if NOT last entry
                        if (i < entries.size() - 1) {
                            writer.write(",");
                        }
                    }
                } catch (NotAuthenticatedException e) {
                    LOGGER.severe("Can not load entries: " + e.getMessage());
                }

                writer.write("]}");
                writer.flush();
            }
        });

        return resourceResponse;
    }
}
