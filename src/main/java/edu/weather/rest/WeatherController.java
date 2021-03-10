package edu.weather.rest;

import edu.weather.rest.model.WeatherInfo;
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

    /**
     * Detects current weather for a particular IP address. If not provided - detects the weather based on the IP of the request.
     *
     * @param ip Specific IP for which to check weather
     * @param request HTTP request, used for default behavior
     * @return Location-based weather information
     */
    @GetMapping()
    public WeatherInfo detectWeather(@RequestParam(required = false) String ip, HttpServletRequest request) {
        String clientIP;
        if (StringUtils.hasLength(ip)) {
            clientIP = ip;
        } else {
            clientIP = RequestUtils.resolveClientIP(request);
        }

        return new WeatherInfo(clientIP);
    }
}
