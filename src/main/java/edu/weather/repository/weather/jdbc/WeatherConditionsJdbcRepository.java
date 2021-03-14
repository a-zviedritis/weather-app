package edu.weather.repository.weather.jdbc;

import edu.weather.repository.weather.WeatherConditionsRepository;
import edu.weather.service.weather.model.IWeatherInfo;
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
public class WeatherConditionsJdbcRepository implements WeatherConditionsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherConditionsJdbcRepository.class);

    private static final String LAST_REPORTED_TIMESTAMP = "select timestamp from weather_condition where latitude = :latitude and longitude = :longitude order by timestamp limit 1";

    private final NamedParameterJdbcOperations parameterJdbcOperations;
    private final SimpleJdbcInsertOperations weatherConditionsInsert;

    @Autowired
    public WeatherConditionsJdbcRepository(DataSource ds) {
        this.parameterJdbcOperations = new NamedParameterJdbcTemplate(ds);

        this.weatherConditionsInsert = new SimpleJdbcInsert(ds)
                .withTableName("weather_condition")
                .usingColumns("longitude", "latitude", "condition", "temperature", "humidity", "wind_speed", "gust_speed", "wind_direction", "timestamp");
    }

    @Override
    public Instant getLastReportedConditionTimestamp(Double latitude, Double longitude) throws Exception {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("longitude", longitude);
        params.addValue("latitude", latitude);

        SqlRowSet rs = executeWithErrorHandling(() -> parameterJdbcOperations.queryForRowSet(LAST_REPORTED_TIMESTAMP, params));

        Instant instant = null;
        if (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("timestamp");
            if (!rs.wasNull()) {
                instant = timestamp.toInstant();
            }
        }

        return instant;
    }

    @Override
    public void saveWeatherConditions(Double latitude, Double longitude, IWeatherInfo weatherInfo) throws Exception {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("longitude", longitude);
        params.addValue("latitude", latitude);
        params.addValue("condition", weatherInfo.getCondition());
        params.addValue("temperature", weatherInfo.getTemperature());
        params.addValue("humidity", weatherInfo.getHumidity());
        params.addValue("wind_speed", weatherInfo.getWindSpeed());
        params.addValue("gust_speed", weatherInfo.getGustSpeed());
        params.addValue("wind_direction", weatherInfo.getWindDirection());
        params.addValue("timestamp", Timestamp.from(Instant.now()), Types.TIMESTAMP);

        executeWithErrorHandling(() -> weatherConditionsInsert.execute(params));
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
