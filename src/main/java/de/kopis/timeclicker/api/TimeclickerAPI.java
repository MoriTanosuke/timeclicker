package de.kopis.timeclicker.api;

import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import de.kopis.timeclicker.model.UserSettings;
import de.kopis.timeclicker.model.wrappers.EntryCount;
import de.kopis.timeclicker.model.wrappers.Project;
import de.kopis.timeclicker.utils.TimeSumUtility;
import de.kopis.timeclicker.utils.TimeclickerEntityFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.DefaultValue;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(name = "timeclicker", version = "v1", scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, "292824132082.apps.googleusercontent.com"},
        audiences = {Constants.ANDROID_AUDIENCE})
public class TimeclickerAPI {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(TimeclickerAPI.class);

    @ApiMethod(name = "delete", path = "delete", httpMethod = "post")
    public void delete(@Named("key") String key, User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        final TimeEntry entry = show(key, user);
        if (entry != null) {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.delete(KeyFactory.stringToKey(entry.getKey()));
            LOGGER.info("User " + user.getUserId() + " deleted entry " + entry.getKey());
        } else {
            LOGGER.warn("No entry found for user {} and key {}", user, key);
        }
    }

    @ApiMethod(name = "stop", path = "stop", httpMethod = "post")
    public TimeEntry stop(@Named("stopKey") String key, User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        try {
            Entity timeEntryEntity = datastore.get(KeyFactory.stringToKey(key));
            if (!timeEntryEntity.getProperty(TimeEntry.ENTRY_USER_ID).equals(user.getUserId())) {
                throw new RuntimeException("Referenced entry does not belong to this user!");
            }
            //TODO check if entry is still open before closing it
            timeEntryEntity.setProperty(TimeEntry.ENTRY_STOP, new Date());
            datastore.put(timeEntryEntity);

            TimeEntry entry = TimeclickerEntityFactory.buildTimeEntryFromEntity(timeEntryEntity);
            LOGGER.info("User " + user.getUserId() + " stopped entry " + timeEntryEntity.getKey());
            return entry;
        } catch (EntityNotFoundException e) {
            LOGGER.warn("Can not find entity with key {}", KeyFactory.stringToKey(key));
        }
        return null;
    }

    @ApiMethod(name = "start", path = "start", httpMethod = "post")
    public TimeEntry start(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // check if there is an open entry and close it
        Entity openEntity = findLatest(user, datastore);
        if (openEntity != null) {
            LOGGER.warn("Open entity found, closing it");
            openEntity.setProperty(TimeEntry.ENTRY_STOP, new Date());
            datastore.put(openEntity);
        }

        // start new entry
        Entity timeEntryEntity = TimeclickerEntityFactory.createTimeEntryEntity(user);
        datastore.put(timeEntryEntity);

        TimeEntry entry = TimeclickerEntityFactory.buildTimeEntryFromEntity(timeEntryEntity);

        LOGGER.info("User " + user.getUserId() + " started a new entry");
        return entry;
    }

    @ApiMethod(name = "show", path = "show")
    public TimeEntry show(@Named("showKey") String key, User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        try {
            Entity timeEntryEntity = datastore.get(KeyFactory.stringToKey(key));
            if (!timeEntryEntity.getProperty(TimeEntry.ENTRY_USER_ID).equals(user.getUserId())) {
                throw new RuntimeException("Referenced entry does not belong to this user!");
            }
            TimeEntry entry = TimeclickerEntityFactory.buildTimeEntryFromEntity(timeEntryEntity);
            LOGGER.info("User " + user.getUserId() + " showed entry " + entry.getKey());
            return entry;
        } catch (EntityNotFoundException e) {
            LOGGER.warn("Can not find entity with key {}", KeyFactory.stringToKey(key));
        }
        return null;
    }

    @ApiMethod(name = "update", path = "update", httpMethod = "post")
    public void update(@Named("key") String key,
                       @Named("start") Date start, @Named("stop") Date stop,
                       @Named("breakDuration") Long breakDuration,
                       @Named("description") String description,
                       @Named("tags") String tags,
                       @Named("project") String project,
                       User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity timeEntryEntity;
            if (key != null && !key.isEmpty()) {
                LOGGER.info("User {} starting to update entry {} with start={} stop={} duration={}", user, key, start, stop, breakDuration);
                timeEntryEntity = datastore.get(KeyFactory.stringToKey(key));

                if (!timeEntryEntity.getProperty(TimeEntry.ENTRY_USER_ID).equals(user.getUserId())) {
                    throw new RuntimeException("Referenced entry does not belong to this user!");
                }
            } else {
                LOGGER.info("User " + user.getUserId() + " starting to save new entry for project=" + project + " with start=" + start + " stop=" + stop);
                timeEntryEntity = TimeclickerEntityFactory.createTimeEntryEntity(user);
            }
            timeEntryEntity.setProperty(TimeEntry.ENTRY_START, start);
            timeEntryEntity.setProperty(TimeEntry.ENTRY_STOP, stop);
            timeEntryEntity.setProperty(TimeEntry.ENTRY_BREAK_DURATION, breakDuration);
            timeEntryEntity.setProperty(TimeEntry.ENTRY_PROJECT, project);
            timeEntryEntity.setProperty(TimeEntry.ENTRY_DESCRIPTION, description);
            timeEntryEntity.setProperty(TimeEntry.ENTRY_TAGS, tags);
            datastore.put(timeEntryEntity);
            LOGGER.info("User " + user.getUserId() + " updated entry " + timeEntryEntity.getKey());
        } catch (EntityNotFoundException e) {
            LOGGER.warn("Can not find entity with key {}", KeyFactory.stringToKey(key));
        }
    }

    @ApiMethod(name = "stopLatest", path = "stop/latest", httpMethod = "post")
    public TimeEntry stopLatest(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity timeEntryEntity = findLatest(user, datastore);
        if (timeEntryEntity == null) {
            LOGGER.warn("No entity found");
            return null;
        }
        LOGGER.debug("Entity found: {}", timeEntryEntity);

        //TODO check if entry is still open before closing it
        timeEntryEntity.setProperty(TimeEntry.ENTRY_STOP, new Date());
        datastore.put(timeEntryEntity);

        TimeEntry entry = TimeclickerEntityFactory.buildTimeEntryFromEntity(timeEntryEntity);
        LOGGER.info("User " + user.getUserId() + " stopped the latest entry");
        return entry;
    }

    /**
     * Returns the latest open {@link TimeEntry} or <code>null</code>.
     *
     * @param user current {@link User}
     * @return latest open {@link TimeEntry} or <code>null</code>
     * @throws NotAuthenticatedException
     */
    @ApiMethod(name = "latest", path = "latest")
    public TimeEntry latest(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity entity = findLatest(user, datastore);
        if (entity != null) {
            TimeEntry timeEntry = TimeclickerEntityFactory.buildTimeEntryFromEntity(entity);
            LOGGER.info("User " + user.getUserId() + " loaded the latest entry");
            return timeEntry;
        }

        return null;
    }

    @ApiMethod(name = "list", path = "list")
    public List<TimeEntry> list(@Named("limit") @DefaultValue("31") int limit, @Named("page") @DefaultValue("0") int page, User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        final List<Entity> entities = listEntities(user, page, limit);

        final List<TimeEntry> l = new ArrayList<>();
        for (Entity timeEntryEntity : entities) {
            final TimeEntry entry = TimeclickerEntityFactory.buildTimeEntryFromEntity(timeEntryEntity);
            l.add(entry);
        }
        LOGGER.info("User " + user.getUserId() + " listed all entries");
        return l;
    }

    @ApiMethod(name = "count", path = "count")
    public EntryCount countAvailableEntries(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        return new EntryCount(countEntities(user));
    }

    @ApiMethod(name = "countDates", path = "count/dates")
    public EntryCount countAvailableDates(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        return new EntryCount(countDates(user));
    }

    @ApiMethod(name = "overallSum", path = "sum/overall")
    public TimeSum getOverallSum(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        TimeSum sum = new TimeSum(Duration.of(0, ChronoUnit.SECONDS));

        List<Entity> entities = listEntities(user);
        for (Entity entity : entities) {
            final Duration duration = calculateDuration(entity);
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
        final TimeSum sum = new TimeSum(Duration.of(0, ChronoUnit.SECONDS));
        for (Entity e : entities) {
            Duration duration = calculateDuration(e);
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
        final TimeSum sum = new TimeSum(Duration.of(0, ChronoUnit.MILLIS));
        for (Entity e : entities) {
            Duration duration = calculateDuration(e);
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
        final TimeSum sum = new TimeSum(Duration.of(0, ChronoUnit.SECONDS));
        for (Entity e : entities) {
            Duration duration = calculateDuration(e);
            sum.addDuration(duration);
        }

        LOGGER.info("User " + user.getUserId() + " calculated daily sum: " + sum);
        return sum;
    }

    @ApiMethod(name = "getUserSettings", path = "settings", httpMethod = "get")
    public UserSettings getUserSettings(@Named("key") String key, User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        LOGGER.info("Searching for settings for user {}", user.getUserId());

        final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity entity = null;
        if (key != null) {
            try {
                entity = datastore.get(KeyFactory.stringToKey(key));
                if (!entity.getProperty(TimeEntry.ENTRY_USER_ID).equals(user.getUserId())) {
                    throw new RuntimeException("Referenced entry does not belong to this user!");
                }
            } catch (EntityNotFoundException e) {
                LOGGER.warn("Can not load user settings for user {} with key {}", user, key);
            }
        } else {
            final Query.Filter propertyFilter =
                    new Query.FilterPredicate(TimeEntry.ENTRY_USER_ID,
                            Query.FilterOperator.EQUAL,
                            user.getUserId());
            final Query q = new Query("UserSettings")
                    .setFilter(propertyFilter);
            final PreparedQuery pq = datastore.prepare(q);
            entity = pq.asSingleEntity();
        }
        UserSettings settings = TimeclickerEntityFactory.buildUserSettingsFromEntity(entity);

        LOGGER.info("Settings found with key={} for user {}", settings.getKey(), user.getUserId());
        return settings;
    }

    @ApiMethod(name = "setUserSettings", path = "settings", httpMethod = "post")
    public void setUserSettings(UserSettings settings, User user) throws NotAuthenticatedException, EntityNotFoundException {
        if (user == null) throw new NotAuthenticatedException();
        LOGGER.info("Updating settings {} for user {}", settings, user.getUserId());

        final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        final String key = settings.getKey();

        Entity entity = null;
        if (key != null) {
            try {
                entity = datastore.get(KeyFactory.stringToKey(key));
                if (!entity.getProperty(TimeEntry.ENTRY_USER_ID).equals(user.getUserId())) {
                    throw new RuntimeException("Referenced entry does not belong to this user!");
                }
                LOGGER.debug("Updating entity with key={}: {}", key, entity);
                // update existing entity
                TimeclickerEntityFactory.updateUserSettingsEntity(user, entity, settings);
            } catch (EntityNotFoundException e) {
                LOGGER.warn("Can not load user settings for user {} with key={}", user, key);
                throw e;
            }
        } else {
            entity = new Entity("UserSettings");
            entity.setProperty("key", settings.getKey());
            TimeclickerEntityFactory.updateUserSettingsEntity(user, entity, settings);
            LOGGER.debug("Creating entity: {}", entity);
        }

        LOGGER.info("Updated settings: {} for user {}", entity, user.getUserId());
        datastore.put(entity);
    }

    @ApiMethod(name = "getProjects", path = "projects", httpMethod = "get")
    public List<Project> getProjects(User user) throws NotAuthenticatedException {
        if (user == null) throw new NotAuthenticatedException();

        final List<Project> projects = new ArrayList<>();

        //TODO collect all used projects
        final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        final Query.Filter propertyFilter =
                new Query.FilterPredicate(TimeEntry.ENTRY_USER_ID,
                        Query.FilterOperator.EQUAL,
                        user.getUserId());
        final Query q = new Query("TimeEntry")
                .setFilter(propertyFilter)
                // only get distinct entries
                .addProjection(new PropertyProjection(TimeEntry.ENTRY_PROJECT, String.class))
                .setDistinct(true);
        PreparedQuery query = datastore.prepare(q);
        final FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
        List<Entity> entities = query.asList(fetchOptions);
        for (Entity e : entities) {
            String project = (String) e.getProperty(TimeEntry.ENTRY_PROJECT);
            projects.add(new Project(project));
        }

        LOGGER.debug("Returning {} projects", projects.size());
        return projects;
    }

    private int countDates(User user) throws NotAuthenticatedException {
        final List<TimeEntry> entities = list(9999, 0, user);
        return new TimeSumUtility().calculateDailyTimeSum(entities).size();
    }

    private int countEntities(User user) {
        final PreparedQuery pq = buildTimeEntryQuery(user);
        final FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
        return pq.countEntities(fetchOptions);
    }

    private List<Entity> listEntities(User user) {
        final PreparedQuery pq = buildTimeEntryQuery(user);
        final FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
        return pq.asList(fetchOptions);
    }

    private List<Entity> listEntities(User user, int page, int pageSize) {
        LOGGER.debug("Listing page={} size={}", page, pageSize);
        final PreparedQuery pq = buildTimeEntryQuery(user);
        final FetchOptions fetchOptions = FetchOptions.Builder.withDefaults().offset(pageSize * page).limit(pageSize);
        return pq.asList(fetchOptions);
    }

    private PreparedQuery buildTimeEntryQuery(final User user) {
        final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // load all time entries for this user
        final Query.Filter propertyFilter =
                new Query.FilterPredicate(TimeEntry.ENTRY_USER_ID,
                        Query.FilterOperator.EQUAL,
                        user.getUserId());
        final Query q = new Query("TimeEntry")
                .setFilter(propertyFilter)
                .addSort(TimeEntry.ENTRY_START, Query.SortDirection.DESCENDING);
        return datastore.prepare(q);
    }

    /**
     * Searches for the latest open entity for the given user.
     *
     * @param user      current {@link User} of the API
     * @param datastore current {@link DatastoreService}
     * @return {@link Entity} if an open entry was found, else <code>null</code>
     */
    private Entity findLatest(User user, DatastoreService datastore) {
        Query.FilterPredicate userFilter = new Query.FilterPredicate(TimeEntry.ENTRY_USER_ID,
                Query.FilterOperator.EQUAL,
                user.getUserId());
        Query.FilterPredicate stopNullFilter = new Query.FilterPredicate(TimeEntry.ENTRY_STOP,
                Query.FilterOperator.EQUAL,
                null);
        Query.Filter propertyFilter = Query.CompositeFilterOperator.and(userFilter, stopNullFilter);
        Query q = new Query("TimeEntry").setFilter(propertyFilter);
        // sort descending to get the newest
        q.addSort(TimeEntry.ENTRY_START, Query.SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(q);
        // only return 1 match
        List<Entity> entities = pq.asList(FetchOptions.Builder.withLimit(1));
        if (entities == null || entities.size() == 0) {
            // no open entity found
            LOGGER.warn("No entity found");
            return null;
        }

        Entity timeEntryEntity = entities.get(0);
        if (!timeEntryEntity.getProperty(TimeEntry.ENTRY_USER_ID).equals(user.getUserId())) {
            throw new RuntimeException("Referenced entry does not belong to this user!");
        }
        return timeEntryEntity;
    }

    private List<Entity> searchTimeEntries(User user, Date firstDate, Date lastDate) {
        // define query where START > FIRST OF MONTH and STOP < LAST OF MONTH
        Query.FilterPredicate userFilter = new Query.FilterPredicate(TimeEntry.ENTRY_USER_ID,
                Query.FilterOperator.EQUAL,
                user.getUserId());
        Query.FilterPredicate startAfter = new Query.FilterPredicate(TimeEntry.ENTRY_START, Query.FilterOperator.GREATER_THAN_OR_EQUAL, firstDate);
        Query.FilterPredicate stopBefore = new Query.FilterPredicate(TimeEntry.ENTRY_START, Query.FilterOperator.LESS_THAN, lastDate);
        Query.Filter propertyFilter = Query.CompositeFilterOperator.and(userFilter, startAfter, stopBefore);
        Query q = new Query("TimeEntry").setFilter(propertyFilter);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);

        // TODO use a limit? FetchOptions.Builder.withLimit(1)
        return pq.asList(FetchOptions.Builder.withDefaults());
    }

    /**
     * Calculates the duration for the given TimeEntry entity. If no {@link TimeEntry#ENTRY_STOP} property is available, the current Date is used.
     *
     * @param entity a {@link TimeEntry}
     * @return duration as <code>long</code> value, calculated from properties {@link TimeEntry#ENTRY_START} and {@link TimeEntry#ENTRY_STOP}
     */
    private Duration calculateDuration(Entity entity) {
        Date start = (Date) entity.getProperty(TimeEntry.ENTRY_START);
        // check if the entity is already stopped, else use the current date
        Date stop;
        if (entity.getProperty(TimeEntry.ENTRY_STOP) != null) {
            stop = (Date) entity.getProperty(TimeEntry.ENTRY_STOP);
        } else {
            stop = new Date();
        }
        long duration = stop.getTime() - start.getTime();

        // check if we need to subtract a break
        if(entity.getProperty(TimeEntry.ENTRY_BREAK_DURATION) != null) {
            duration -= (Long) entity.getProperty(TimeEntry.ENTRY_BREAK_DURATION);
        }

        return Duration.of(duration, ChronoUnit.MILLIS);
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
