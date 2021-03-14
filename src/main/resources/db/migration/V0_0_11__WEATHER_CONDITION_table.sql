CREATE TABLE WEATHER_CONDITION (
    latitude double NOT NULL,
    longitude double NOT NULL,
    timestamp timestamp NOT NULL,
    condition varchar NOT NULL,
    temperature double NOT NULL,
    humidity int NOT NULL,
    wind_speed double NOT NULL,
    gust_speed double NOT NULL,
    wind_direction varchar NOT NULL,
    PRIMARY KEY (latitude, longitude, timestamp)
);