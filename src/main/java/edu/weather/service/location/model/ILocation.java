package edu.weather.service.location.model;

/**
 * @author andris
 * @since 1.0.0
 */
public interface ILocation {

    String getContinent();
    String getCountry();
    String getCity();
    Double getLatitude();
    Double getLongitude();
}
