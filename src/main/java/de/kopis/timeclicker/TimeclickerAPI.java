package de.kopis.timeclicker;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Api(name = "timeclicker", version = "v1", scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, "292824132082.apps.googleusercontent.com"},
        audiences = {Constants.ANDROID_AUDIENCE})
public class TimeclickerAPI {
    private static final Logger LOGGER = Logger.getLogger(TimeclickerAPI.class.getName());

    @ApiMethod(name = "start", path = "start", httpMethod = "post")
    public TimeEntry start(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // TODO check if there is an open entry and close it
        Entity openEntity = findLatest(user, datastore);
        if (openEntity != null) {
            LOGGER.warning("Open entity found, closing it");
            openEntity.setProperty("stop", new Date());
            datastore.put(openEntity);
        }

        // start new entry
        Entity timeEntryEntity = new Entity("TimeEntry");
        timeEntryEntity.setProperty("start", new Date());
        // set stop=null to make if queriable
        timeEntryEntity.setProperty("stop", null);
        timeEntryEntity.setProperty("userId", user.getUserId());
        datastore.put(timeEntryEntity);

        TimeEntry entry = buildTimeEntryFromEntity(timeEntryEntity);

        LOGGER.info("User " + user.getUserId() + " started a new entry");
        return entry;
    }

    @ApiMethod(name = "stopLatest", path = "stop/latest", httpMethod = "post")
    public TimeEntry stopLatest(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity timeEntryEntity = findLatest(user, datastore);
        if (timeEntryEntity == null) {
            LOGGER.warning("No entity found");
            return null;
        }
        LOGGER.fine("Entity found: " + timeEntryEntity);

        //TODO check if entry is still open before closing it
        timeEntryEntity.setProperty("stop", new Date());
        datastore.put(timeEntryEntity);

        TimeEntry entry = buildTimeEntryFromEntity(timeEntryEntity);
        LOGGER.info("User " + user.getUserId() + " stopped the latest entry");
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
            LOGGER.info("User " + user.getUserId() + " stopped entry " + timeEntryEntity.getKey());
            return entry;
        } catch (EntityNotFoundException e) {
            LOGGER.warning("Can not find entity with key " + KeyFactory.stringToKey(key));
        }
        return null;
    }

    @ApiMethod(name = "latest", path = "latest")
    public TimeEntry latest(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity entity = findLatest(user, datastore);
        if (entity != null) {
            TimeEntry timeEntry = buildTimeEntryFromEntity(entity);
            LOGGER.info("User " + user.getUserId() + " loaded the latest entry");
            return timeEntry;
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
            LOGGER.info("User " + user.getUserId() + " showed entry " + entry.getKey());
            return entry;
        } catch (EntityNotFoundException e) {
            LOGGER.warning("Can not find entity with key " + KeyFactory.stringToKey(key));
        }
        return null;
    }

    @ApiMethod(name = "list", path = "list")
    public List<TimeEntry> list(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        List<Entity> entities = listEntities(user);

        List<TimeEntry> l = new ArrayList<>();
        for (Entity timeEntryEntity : entities) {
            TimeEntry entry = buildTimeEntryFromEntity(timeEntryEntity);
            l.add(entry);
        }
        LOGGER.info("User " + user.getUserId() + " listed all entries");
        return l;
    }

    @ApiMethod(name = "overallSum", path = "sum/overall")
    public TimeSum getOverallSum(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        TimeSum sum = new TimeSum();

        List<Entity> entities = listEntities(user);
        for (Entity entity : entities) {
            // default to current timestamp
            Date stop = new Date();
            // try to load stop date from entry
            if (entity.getProperty("stop") != null) {
                stop = (Date) entity.getProperty("stop");
            }
            Date start = (Date) entity.getProperty("start");

            long duration = stop.getTime() - start.getTime();
            sum.setDuration(sum.getDuration() + duration);
        }

        LOGGER.info("User " + user.getUserId() + " calculated overall sum: " + sum);
        return sum;
    }

    @ApiMethod(name = "monthlySum", path = "sum/monthly")
    public TimeSum getMonthlySum(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        TimeSum sum = new TimeSum();
        //TODO calculate sum for this month

        // get first of month
        final Calendar cal = Calendar.getInstance();
        // set everything to 0
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.set(Calendar.DAY_OF_MONTH, 0);
        final Date firstOfMonth = cal.getTime();
        // get first of next month
        cal.add(Calendar.MONTH, 1);
        final Date firstOfNextMonth = cal.getTime();
        //TODO define query where START > FIRST OF MONTH and STOP < LAST OF MONTH

        Query.FilterPredicate userFilter = new Query.FilterPredicate("userId",
                Query.FilterOperator.EQUAL,
                user.getUserId());
        Query.FilterPredicate startAfterFirstOfMonth = new Query.FilterPredicate("start", Query.FilterOperator.GREATER_THAN_OR_EQUAL, firstOfMonth);
        Query.FilterPredicate stopBeforeLastOfMonth = new Query.FilterPredicate("start", Query.FilterOperator.LESS_THAN, firstOfNextMonth);
        Query.Filter propertyFilter = Query.CompositeFilterOperator.and(userFilter, startAfterFirstOfMonth, stopBeforeLastOfMonth);
        Query q = new Query("TimeEntry").setFilter(propertyFilter);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        // only return 1 match
        List<Entity> entities = pq.asList(FetchOptions.Builder.withLimit(1));

        return sum;
    }

    @ApiMethod(name = "weeklySum", path = "sum/weekly")
    public TimeSum getWeeklySum(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        TimeSum sum = new TimeSum();
        //TODO calculate sum for this week

        return sum;
    }

    @ApiMethod(name = "dailySum", path = "sum/daily")
    public TimeSum getDailySum(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        TimeSum sum = new TimeSum();
        //TODO calculate sum for today

        return sum;
    }

    private List<Entity> listEntities(User user) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // load all time entries for this user
        Query.Filter propertyFilter =
                new Query.FilterPredicate("userId",
                        Query.FilterOperator.EQUAL,
                        user.getUserId());
        Query q = new Query("TimeEntry").setFilter(propertyFilter);
        PreparedQuery pq = datastore.prepare(q);
        //TODO remove limit?
        return pq.asList(FetchOptions.Builder.withLimit(100));
    }

    /**
     * Searches for the latest open entity for the given user.
     *
     * @param user      current {@link User} of the API
     * @param datastore current {@link DatastoreService}
     * @return {@link Entity} if an open entry was found, else <code>null</code>
     */
    private Entity findLatest(User user, DatastoreService datastore) {
        Query.FilterPredicate userFilter = new Query.FilterPredicate("userId",
                Query.FilterOperator.EQUAL,
                user.getUserId());
        Query.FilterPredicate stopNullFilter = new Query.FilterPredicate("stop",
                Query.FilterOperator.EQUAL,
                null);
        Query.Filter propertyFilter = Query.CompositeFilterOperator.and(userFilter, stopNullFilter);
        Query q = new Query("TimeEntry").setFilter(propertyFilter);
        // sort descending to get the newest
        q.addSort("start", Query.SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(q);
        // only return 1 match
        List<Entity> entities = pq.asList(FetchOptions.Builder.withLimit(1));
        if (entities == null || entities.size() == 0) {
            // no open entity found
            LOGGER.warning("No entity found");
            return null;
        }

        Entity timeEntryEntity = entities.get(0);
        if (!timeEntryEntity.getProperty("userId").equals(user.getUserId())) {
            throw new RuntimeException("Referenced entry does not belong to this user!");
        }
        return timeEntryEntity;
    }

    private TimeEntry buildTimeEntryFromEntity(Entity timeEntryEntity) {
        TimeEntry entry = new TimeEntry();
        entry.setKey(KeyFactory.keyToString(timeEntryEntity.getKey()));
        if (timeEntryEntity.hasProperty("start")) {
            entry.setStart((Date) timeEntryEntity.getProperty("start"));
        }
        if (timeEntryEntity.hasProperty("stop")) {
            entry.setStop((Date) timeEntryEntity.getProperty("stop"));
        }
        return entry;
    }
}
