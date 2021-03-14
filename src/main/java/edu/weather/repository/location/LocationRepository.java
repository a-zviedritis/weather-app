package edu.weather.repository.location;

import edu.weather.service.location.model.ILocation;

/**
 * Geolocation information repository interface.
 *
 * @author andris
 * @since 1.0.0
 */
public interface LocationRepository {

    /**
     * Determines whether a geolocation entry has been saved with the provided IP address.
     *
     * @param ip IP address to check
     * @return true/false
     */
    boolean locationExists(String ip) throws Exception;

    /**
     * Saves a particular location entry for the provided IP address.
     *
     * @param ip IP address for which to save the location
     * @param location Geolocation information
     */
    void saveLocation(String ip, ILocation location) throws Exception;

    /**
     * Saves a geolocation access instance.
     *
     * @param ip IP address for which access has been made
     */
    void auditLogAccess(String ip) throws Exception;
}
