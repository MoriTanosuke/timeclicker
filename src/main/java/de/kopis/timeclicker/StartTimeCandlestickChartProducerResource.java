package de.kopis.timeclicker;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.string.StringValue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class StartTimeCandlestickChartProducerResource extends AbstractResource {
    private static final Logger LOGGER = Logger.getLogger(StartTimeCandlestickChartProducerResource.class.getName());

    private static final TimeclickerAPI api = new TimeclickerAPI();
    private int pageSize = 31;

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
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
                    final List<TimeEntry> entries = api.list(99999, currentUser);
                    // sort ascending
                    Collections.sort(entries, new Comparator<TimeEntry>() {
                        @Override
                        public int compare(TimeEntry o1, TimeEntry o2) {
                            // sort ASC by start date
                            return o1.getStart().compareTo(o2.getStart());
                        }
                    });

                    LOGGER.info("Found " + entries.size() + " entries...");
                    for (int i = 0; i < entries.size(); i++) {
                        final TimeEntry entry = entries.get(i);
                        // need to convert to ISO8601 for javascript
                        writer.write("[" + entry.getStart().getTime() +
                                "," + (entry.getStart().getTime() % (24 * 60 * 60 * 1000)) +
                                "," + (entry.getStop().getTime() % (24 * 60 * 60 * 1000)) +
                                "]");
                        // only write "," if NOT last entry
                        if (i < entries.size() - 1) {
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
}
