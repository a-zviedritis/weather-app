package edu.weather.service.weather.exception;

/**
 * @author andris
 * @since 1.0.0
 */
public class LocationNotFoundException extends Exception {

    public LocationNotFoundException(String msg) {
        super(msg);
    }
}
