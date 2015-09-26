package de.kopis.timeclicker;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(name = "timeclicker", version = "v1", scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, "292824132082.apps.googleusercontent.com"},
        audiences = {Constants.ANDROID_AUDIENCE})
public class YourFirstAPI {
    @ApiMethod(name = "start", path = "start", httpMethod = "post")
    public TimeEntry start(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // TODO check if there is an open entry and close it

        // start new entry
        Entity timeEntryEntity = new Entity("TimeEntry");
        timeEntryEntity.setProperty("start", new Date());
        timeEntryEntity.setProperty("userId", user.getUserId());
        datastore.put(timeEntryEntity);

        TimeEntry entry = buildTimeEntryFromEntity(timeEntryEntity);

        return entry;
    }

    @ApiMethod(name = "stop", path = "stop", httpMethod = "post")
    public TimeEntry stop(@Named("key") String key, User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        try {
            Entity timeEntryEntity = datastore.get(KeyFactory.stringToKey(key));
            if (!timeEntryEntity.getProperty("userId").equals(user.getUserId())) {
                throw new RuntimeException("Referenced entry does not belong to this user!");
            }
            //TODO check if entry is still open before closing it
            timeEntryEntity.setProperty("stop", new Date());
            datastore.put(timeEntryEntity);

            TimeEntry entry = buildTimeEntryFromEntity(timeEntryEntity);
            return entry;
        } catch (EntityNotFoundException e) {
            System.err.println("Can not find entity with key " + KeyFactory.stringToKey(key));
        }
        return null;
    }

    @ApiMethod(name = "show", path = "show")
    public TimeEntry show(@Named("key") String key, User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        try {
            Entity timeEntryEntity = datastore.get(KeyFactory.stringToKey(key));
            if (!timeEntryEntity.getProperty("userId").equals(user.getUserId())) {
                throw new RuntimeException("Referenced entry does not belong to this user!");
            }
            TimeEntry entry = buildTimeEntryFromEntity(timeEntryEntity);
            return entry;
        } catch (EntityNotFoundException e) {
            System.err.println("Can not find entity with key " + KeyFactory.stringToKey(key));
        }
        return null;
    }

    @ApiMethod(name = "list", path = "list")
    public List<TimeEntry> list(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        //TODO load all time entries for this user
        Query.Filter propertyFilter =
                new Query.FilterPredicate("userId",
                        Query.FilterOperator.EQUAL,
                        user.getUserId());
        Query q = new Query("TimeEntry").setFilter(propertyFilter);
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> entities = pq.asList(FetchOptions.Builder.withLimit(5));

        List<TimeEntry> l = new ArrayList<>();
        for (Entity timeEntryEntity : entities) {
            TimeEntry entry = buildTimeEntryFromEntity(timeEntryEntity);
            l.add(entry);
        }
        return l;
    }

    private TimeEntry buildTimeEntryFromEntity(Entity timeEntryEntity) {
        TimeEntry entry = new TimeEntry();
        entry.setKey(KeyFactory.keyToString(timeEntryEntity.getKey()));
        entry.setStart((Date) timeEntryEntity.getProperty("start"));
        entry.setStop((Date) timeEntryEntity.getProperty("stop"));
        return entry;
    }
}
