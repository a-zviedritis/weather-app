package edu.weather.repository.location.jdbc;

import edu.weather.repository.location.LocationRepository;
import edu.weather.service.location.model.ILocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcInsertOperations;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * @author andris
 * @since 1.0.0
 */
@Repository
public class LocationJdbcRepository implements LocationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationJdbcRepository.class);

    private static final String SELECT_EVERYTHING_FROM_GEOLOCATION = "select continent, country, city, longitude, latitude from geolocation where ip = :ip";
    private static final String CHECK_IF_LOCATION_EXISTS = String.format("select 1 from geolocation where exists (%s)", SELECT_EVERYTHING_FROM_GEOLOCATION);

    private final NamedParameterJdbcOperations parameterJdbcOperations;
    private final SimpleJdbcInsertOperations locationInsert;
    private final SimpleJdbcInsertOperations locationAccessInsert;

    @Autowired
    public LocationJdbcRepository(DataSource ds) {
        this.parameterJdbcOperations = new NamedParameterJdbcTemplate(ds);

        locationInsert = new SimpleJdbcInsert(ds)
                .withTableName("geolocation")
                .usingColumns("ip", "continent", "country", "city", "longitude", "latitude");
        locationAccessInsert = new SimpleJdbcInsert(ds)
                .withTableName("geolocation_access")
                .usingColumns("ip", "timestamp");
    }

    @Override
    public boolean locationExists(String ip) throws Exception {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ip", ip);

        SqlRowSet rs = executeWithErrorHandling(() -> parameterJdbcOperations.queryForRowSet(CHECK_IF_LOCATION_EXISTS, params));
        return rs.next();
    }

    @Override
    public void saveLocation(String ip, ILocation location) throws Exception {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ip", ip);
        params.addValue("continent", location.getContinent());
        params.addValue("country", location.getCountry());
        params.addValue("city", location.getCity());
        params.addValue("longitude", location.getLongitude());
        params.addValue("latitude", location.getLatitude());

        executeWithErrorHandling(() -> locationInsert.execute(params));
    }

    @Override
    public void auditLogAccess(String ip) throws Exception {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ip", ip);
        params.addValue("timestamp", Timestamp.from(Instant.now()), Types.TIMESTAMP);

        executeWithErrorHandling(() -> locationAccessInsert.execute(params));
    }

    private <T> T executeWithErrorHandling(Supplier<T> action) throws Exception {
        try {
            return action.get();
        } catch (DataAccessException ex) {
            LOGGER.error("SQL error: {}", ex.getMessage());
            throw new Exception(ex);
        }
    }
}
