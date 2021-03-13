package edu.weather.rest;

import edu.weather.rest.mapper.LocationMapper;
import edu.weather.rest.mapper.WeatherMapper;
import edu.weather.rest.model.WeatherResponse;
import edu.weather.service.WeatherService;
import edu.weather.service.location.model.ILocation;
import edu.weather.service.weather.model.IWeatherInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * REST controller for weather related operations
 *
 * @author andris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final LocationMapper locationMapper;
    private final WeatherMapper weatherMapper;

    @Autowired
    public WeatherController(WeatherService weatherService,
                             LocationMapper locationMapper,
                             WeatherMapper weatherMapper) {
        this.weatherService = weatherService;
        this.locationMapper = locationMapper;
        this.weatherMapper = weatherMapper;
    }

    /**
     * Detects current weather for a particular IP address. If not provided - detects the weather based on the IP of the request.
     *
     * @param ip Specific IP for which to check weather
     * @param request HTTP request, used for default behavior
     * @return Location-based weather information
     */
    @GetMapping
    public WeatherResponse detectWeather(@RequestParam(required = false) String ip, HttpServletRequest request) {
        String clientIP;
        if (StringUtils.hasLength(ip)) {
            clientIP = ip;
        } else {
            clientIP = RequestUtils.resolveClientIP(request);
        }

        Pair<ILocation, IWeatherInfo> results = weatherService.detectWeather(clientIP);

        WeatherResponse info = new WeatherResponse(clientIP);
        info.setLocation(locationMapper.toDTO(results.getKey()));
        info.setWeather(weatherMapper.toDTO(results.getValue()));
        return info;
    }
}
