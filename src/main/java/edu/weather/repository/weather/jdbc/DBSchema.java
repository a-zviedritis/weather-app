package edu.weather.repository.weather.jdbc;

/**
 * Weather information specific database schema
 *
 * @author andris
 * @since 1.0.0
 */
class DBSchema {

    private DBSchema() {
        // no instances of this
    }

    public static class WeatherConditionTable {

        public static final String TABLE_NAME = "WEATHER_CONDITION";

        public static final String COLUMN_LONGITUDE = "LONGITUDE";
        public static final String COLUMN_LATITUDE = "LATITUDE";
        public static final String COLUMN_TIMESTAMP = "TIMESTAMP";
        public static final String COLUMN_CONDITION = "CONDITION";
        public static final String COLUMN_TEMPERATURE = "TEMPERATURE";
        public static final String COLUMN_HUMIDITY = "HUMIDITY";
        public static final String COLUMN_WIND_SPEED = "WIND_SPEED";
        public static final String COLUMN_GUST_SPEED = "GUST_SPEED";
        public static final String COLUMN_WIND_DIRECTION = "WIND_DIRECTION";
    }

}
