package edu.weather.service.weather.exception;

/**
 * @author andris
 * @since 1.0.0
 */
public class WeatherDetectionException extends Exception {

    public WeatherDetectionException() {
        super();
    }

    public WeatherDetectionException(String msg) {
        super(msg);
    }

    public WeatherDetectionException(Throwable t) {
        super(t);
    }
}
