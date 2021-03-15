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

import static edu.weather.repository.weather.jdbc.DBSchema.WeatherConditionTable;

/**
 * @author andris
 * @since 1.0.0
 */
@Repository
public class WeatherConditionsJdbcRepository implements WeatherConditionsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherConditionsJdbcRepository.class);

    private static final String LAST_REPORTED_TIMESTAMP =
            String.format("select timestamp from weather_condition where latitude = :%s and longitude = :%s order by timestamp limit 1",
                    WeatherConditionTable.COLUMN_LATITUDE, WeatherConditionTable.COLUMN_LONGITUDE);

    private final NamedParameterJdbcOperations parameterJdbcOperations;
    private final SimpleJdbcInsertOperations weatherConditionsInsert;

    @Autowired
    public WeatherConditionsJdbcRepository(DataSource ds) {
        this.parameterJdbcOperations = new NamedParameterJdbcTemplate(ds);

        this.weatherConditionsInsert = new SimpleJdbcInsert(ds)
                .withTableName(WeatherConditionTable.TABLE_NAME)
                .usingColumns(
                        WeatherConditionTable.COLUMN_LONGITUDE,
                        WeatherConditionTable.COLUMN_LATITUDE,
                        WeatherConditionTable.COLUMN_CONDITION,
                        WeatherConditionTable.COLUMN_TEMPERATURE,
                        WeatherConditionTable.COLUMN_HUMIDITY,
                        WeatherConditionTable.COLUMN_WIND_SPEED,
                        WeatherConditionTable.COLUMN_GUST_SPEED,
                        WeatherConditionTable.COLUMN_WIND_DIRECTION,
                        WeatherConditionTable.COLUMN_TIMESTAMP
                );
    }

    @Override
    public Instant getLastReportedConditionTimestamp(Double latitude, Double longitude) throws Exception {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(WeatherConditionTable.COLUMN_LONGITUDE, longitude);
        params.addValue(WeatherConditionTable.COLUMN_LATITUDE, latitude);

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
        params.addValue(WeatherConditionTable.COLUMN_LONGITUDE, longitude);
        params.addValue(WeatherConditionTable.COLUMN_LATITUDE, latitude);
        params.addValue(WeatherConditionTable.COLUMN_CONDITION, weatherInfo.getCondition());
        params.addValue(WeatherConditionTable.COLUMN_TEMPERATURE, weatherInfo.getTemperature());
        params.addValue(WeatherConditionTable.COLUMN_HUMIDITY, weatherInfo.getHumidity());
        params.addValue(WeatherConditionTable.COLUMN_WIND_SPEED, weatherInfo.getWindSpeed());
        params.addValue(WeatherConditionTable.COLUMN_GUST_SPEED, weatherInfo.getGustSpeed());
        params.addValue(WeatherConditionTable.COLUMN_WIND_DIRECTION, weatherInfo.getWindDirection());
        params.addValue(WeatherConditionTable.COLUMN_TIMESTAMP, Timestamp.from(Instant.now()), Types.TIMESTAMP);

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
