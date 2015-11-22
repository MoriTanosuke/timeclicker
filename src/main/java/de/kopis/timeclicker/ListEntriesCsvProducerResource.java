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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class ListEntriesCsvProducerResource extends AbstractResource {
    private static final Logger LOGGER = Logger.getLogger(ListEntriesChartProducerResource.class.getName());

    private static final transient TimeclickerAPI api = new TimeclickerAPI();

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        final ResourceResponse resourceResponse = new ResourceResponse();
        resourceResponse.setContentType("application/text");
        resourceResponse.setTextEncoding("utf-8");
        resourceResponse.setFileName("list.csv");

        resourceResponse.setWriteCallback(new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) throws IOException {
                final UserService userService = UserServiceFactory.getUserService();
                final User currentUser = userService.getCurrentUser();

                final OutputStream outputStream = attributes.getResponse().getOutputStream();
                final Writer writer = new OutputStreamWriter(outputStream);


                try {
                    final List<TimeEntry> entries = api.list(currentUser);
                    // sort ascending
                    Collections.sort(entries, new Comparator<TimeEntry>() {
                        @Override
                        public int compare(TimeEntry o1, TimeEntry o2) {
                            // sort ASC by start date
                            return o1.getStart().compareTo(o2.getStart());
                        }
                    });

                    writer.write("Date,Start,Stop,Sum\n");
                    for (TimeEntry entry : entries) {
                        writer.write("\"" +
                                new DateTime(entry.getStart()).toString("YYYY-MM-dd") + "\",\"" +
                                new DateTime(entry.getStart()).toString("HH:mm:ss") + "\",\"" +
                                new DateTime(entry.getStop()).toString("HH:mm:ss") + "\"," +
                                new TimeSum(entry).getDuration() + "\n");
                    }
                } catch (NotAuthenticatedException e) {
                    LOGGER.severe("Can not load entries: " + e.getMessage());
                }

                writer.flush();
                writer.close();
            }
        });

        return resourceResponse;
    }
}
