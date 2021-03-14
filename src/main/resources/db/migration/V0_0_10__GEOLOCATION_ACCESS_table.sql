CREATE TABLE GEOLOCATION_ACCESS (
    ip varchar NOT NULL,
    timestamp timestamp NOT NULL,
    PRIMARY KEY (ip, timestamp),
    FOREIGN KEY (ip) REFERENCES GEOLOCATION(ip)
);