package de.kopis.timeclicker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.string.StringValue;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;

public class ListEntriesChartProducerResource extends AbstractResource {
    private static final Logger LOGGER = Logger.getLogger(ListEntriesChartProducerResource.class.getName());

    private static final TimeclickerAPI api = new TimeclickerAPI();
    private int pageSize = 31;

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        if (attributes.getParameters().get("pageSize") != null) {
            final StringValue ps = attributes.getParameters().get("pageSize");
            pageSize = ps.toInt(31);
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

                writer.write("{\"cols\":[" +
                        "{\"label\":\"Tag\",\"type\":\"string\"}," +
                        "{\"label\":\"Tracked\",\"type\":\"number\"}" +
                        "],\"rows\":[");

                try {
                    final List<TimeEntry> entries = api.list(pageSize, currentUser);
                    // sort ascending
                    Collections.sort(entries, new Comparator<TimeEntry>() {
                        @Override
                        public int compare(TimeEntry o1, TimeEntry o2) {
                            // sort ASC by start date
                            return o1.getStart().compareTo(o2.getStart());
                        }
                    });

                    final Map<String, TimeSum> timeByTags = new HashMap<>();
                    for (int i = 0; i < entries.size(); i++) {
                        final TimeEntry entry = entries.get(i);
                        final String tags = entry.getTags() != null ? entry.getTags() : "no tag";
                        if (!timeByTags.containsKey(tags)) {
                            timeByTags.put(tags, new TimeSum(0));
                        }
                        final TimeSum sum = timeByTags.get(tags);
                        sum.add(new TimeSum(entry));
                    }

                    // write by tag
                    final String[] keys = timeByTags.keySet().toArray(new String[0]);
                    for (int i = 0; i < keys.length; i++) {
                        final String tag = keys[i];
                        final TimeSum sum = timeByTags.get(tag);
                        writer.write("{\"c\":[" +
                                "{\"v\":\"" + tag + "\"}," +
                                "{\"v\":" + sum.getDuration() + "}" +
                                "]}");
                        // only write "," if NOT last entry
                        if (i < keys.length - 1) {
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
