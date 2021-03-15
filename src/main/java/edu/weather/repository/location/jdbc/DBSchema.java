package edu.weather.repository.location.jdbc;

/**
 * Geolocation specific database schema
 *
 * @author andris
 * @since 1.0.0
 */
class DBSchema {

    private DBSchema() {
        // no instances of this
    }

    public static class GeolocationTable {

        public static final String TABLE_NAME = "GEOLOCATION";

        public static final String COLUMN_IP = "IP";
        public static final String COLUMN_CONTINENT = "CONTINENT";
        public static final String COLUMN_COUNTRY = "COUNTRY";
        public static final String COLUMN_CITY = "CITY";
        public static final String COLUMN_LONGITUDE = "LONGITUDE";
        public static final String COLUMN_LATITUDE = "LATITUDE";
    }

    public static class GeolocationAccessTable {

        public static final String TABLE_NAME = "GEOLOCATION_ACCESS";

        public static final String COLUMN_IP = "IP";
        public static final String COLUMN_TIMESTAMP = "TIMESTAMP";
    }

}
