package de.kopis.timeclicker.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Api(name = "timeclicker", version = "v1", scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, "292824132082.apps.googleusercontent.com"},
        audiences = {Constants.ANDROID_AUDIENCE})
public class TimeclickerAPI {
    private static final transient Logger LOGGER = Logger.getLogger(TimeclickerAPI.class.getName());

    @ApiMethod(name = "delete", path = "delete", httpMethod = "post")
    public void delete(@Named("key") String key, User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        final TimeEntry entry = show(key, user);
        if (entry != null) {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.delete(KeyFactory.stringToKey(entry.getKey()));
            LOGGER.info("User " + user.getUserId() + " deleted entry " + entry.getKey());
        } else {
            LOGGER.warning("No entry found for user " + user + " and key " + key);
        }
    }

    @ApiMethod(name = "stop", path = "stop", httpMethod = "post")
    public TimeEntry stop(@Named("stopKey") String key, User user) throws NotAuthenticatedException {
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

    @ApiMethod(name = "show", path = "show")
    public TimeEntry show(@Named("showKey") String key, User user) throws NotAuthenticatedException {
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

    @ApiMethod(name = "update", path = "update")
    public void update(@Named("key") String key, Date start, Date stop, User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        try {
            Entity timeEntryEntity = datastore.get(KeyFactory.stringToKey(key));
            if (!timeEntryEntity.getProperty("userId").equals(user.getUserId())) {
                throw new RuntimeException("Referenced entry does not belong to this user!");
            }
            timeEntryEntity.setProperty("start", start);
            timeEntryEntity.setProperty("stop", stop);
            datastore.put(timeEntryEntity);
            LOGGER.info("User " + user.getUserId() + " updated entry " + timeEntryEntity.getKey());
        } catch (EntityNotFoundException e) {
            LOGGER.warning("Can not find entity with key " + KeyFactory.stringToKey(key));
        }
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
            final long duration = calculateDuration(entity);
            sum.addDuration(duration);
        }

        LOGGER.info("User " + user.getUserId() + " calculated overall sum: " + sum);
        return sum;
    }

    @ApiMethod(name = "monthlySum", path = "sum/monthly")
    public TimeSum getMonthlySum(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        // get first of month
        final Calendar cal = getCalendar();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        final Date firstDate = cal.getTime();
        // get first of next month
        cal.add(Calendar.MONTH, 1);
        final Date lastDate = cal.getTime();
        LOGGER.info("Searching from " + firstDate + " to " + lastDate);
        final List<Entity> entities = searchTimeEntries(user, firstDate, lastDate);


        // calculate the sum from the result list
        final TimeSum sum = new TimeSum();
        for (Entity e : entities) {
            long duration = calculateDuration(e);
            sum.addDuration(duration);
        }

        LOGGER.info("User " + user.getUserId() + " calculated monthly sum: " + sum);
        return sum;
    }

    @ApiMethod(name = "weeklySum", path = "sum/weekly")
    public TimeSum getWeeklySum(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        final Calendar cal = getCalendar();

        // get first of week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        final Date firstDate = cal.getTime();
        // get first of next week
        cal.add(Calendar.DAY_OF_YEAR, 7);
        final Date lastDate = cal.getTime();
        LOGGER.info("Searching from " + firstDate + " to " + lastDate);
        final List<Entity> entities = searchTimeEntries(user, firstDate, lastDate);


        // calculate the sum from the result list
        final TimeSum sum = new TimeSum();
        for (Entity e : entities) {
            long duration = calculateDuration(e);
            sum.addDuration(duration);
        }

        LOGGER.info("User " + user.getUserId() + " calculated weekly sum: " + sum);
        return sum;
    }

    @ApiMethod(name = "dailySum", path = "sum/daily")
    public TimeSum getDailySum(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        final Calendar cal = getCalendar();
        final Date firstDate = cal.getTime();
        // get tomorrow
        cal.add(Calendar.DAY_OF_YEAR, 1);
        final Date lastDate = cal.getTime();
        LOGGER.info("Searching from " + firstDate + " to " + lastDate);
        final List<Entity> entities = searchTimeEntries(user, firstDate, lastDate);


        // calculate the sum from the result list
        final TimeSum sum = new TimeSum();
        for (Entity e : entities) {
            long duration = calculateDuration(e);
            sum.addDuration(duration);
        }

        LOGGER.info("User " + user.getUserId() + " calculated daily sum: " + sum);
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

    private List<Entity> searchTimeEntries(User user, Date firstDate, Date lastDate) {
        // define query where START > FIRST OF MONTH and STOP < LAST OF MONTH
        Query.FilterPredicate userFilter = new Query.FilterPredicate("userId",
                Query.FilterOperator.EQUAL,
                user.getUserId());
        Query.FilterPredicate startAfter = new Query.FilterPredicate("start", Query.FilterOperator.GREATER_THAN_OR_EQUAL, firstDate);
        Query.FilterPredicate stopBefore = new Query.FilterPredicate("start", Query.FilterOperator.LESS_THAN, lastDate);
        Query.Filter propertyFilter = Query.CompositeFilterOperator.and(userFilter, startAfter, stopBefore);
        Query q = new Query("TimeEntry").setFilter(propertyFilter);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);

        // TODO use a limit? FetchOptions.Builder.withLimit(1)
        return pq.asList(FetchOptions.Builder.withDefaults());
    }

    /**
     * Calculates the duration for the given TimeEntry entity. If no "stop" property is available, the current Date is used.
     *
     * @param entity a {@link TimeEntry}
     * @return duration as <code>long</code> value, calculated from properties "start" and "stop"
     */
    private long calculateDuration(Entity entity) {
        Date start = (Date) entity.getProperty("start");
        // check if the entity is already stopped, else use the current date
        Date stop;
        if (entity.getProperty("stop") != null) {
            stop = (Date) entity.getProperty("stop");
        } else {
            stop = new Date();
        }

        return stop.getTime() - start.getTime();
    }

    /**
     * Returns a calendar with HOUR, MINUTE, SECOND, MILLISECOND set to ZERO.
     *
     * @return {@link Calendar}
     */
    private Calendar getCalendar() {
        final Calendar cal = Calendar.getInstance();
        // set everything to 0
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
}