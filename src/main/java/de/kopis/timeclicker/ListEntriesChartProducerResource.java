package de.kopis.timeclicker;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import org.apache.wicket.request.resource.AbstractResource;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Logger;

public class ListEntriesChartProducerResource extends AbstractResource {
    private static final Logger LOGGER = Logger.getLogger(ListEntriesChartProducerResource.class.getName());

    private static final TimeclickerAPI api = new TimeclickerAPI();

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse resourceResponse = new ResourceResponse();
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
                        "{\"label\":\"Start\",\"type\":\"string\"}," +
                        "{\"label\":\"Tracked\",\"type\":\"number\"}" +
                        "],\"rows\":[");

                try {
                    final List<TimeEntry> entries = api.list(currentUser);
                    for (int i = 0; i < entries.size(); i++) {
                        final TimeEntry entry = entries.get(i);
                        if (entry.getStart() != null && entry.getStop() != null) {
                            // need to convert to ISO8601 for javascript
                            writer.write("{\"c\":[" +
                                    "{\"v\":\"" + new DateTime(entry.getStart()) + "\"}," +
                                    "{\"v\":" + new TimeSum(entry).getDuration() + "}" +
                                    "]}");
                        } else {
                            LOGGER.fine("Skipping entry " + entry);
                        }
                        // only write , if NOT last entry
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
