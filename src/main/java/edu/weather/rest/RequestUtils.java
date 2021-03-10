package edu.weather.rest;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

/**
 * @author andris
 * @since 1.0.0
 */
public class RequestUtils {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private RequestUtils() {
        // no instances of this
    }

    /**
     * Resolves the IP address from which the request has originated.
     *
     * @param request HTTP request
     * @return Client IP address
     */
    public static String resolveClientIP(HttpServletRequest request) {
        return Stream.of(IP_HEADER_CANDIDATES)
                .map(request::getHeader)
                .filter(v -> StringUtils.hasLength(v) && !"unknown".equalsIgnoreCase(v))
                .findFirst()
                .map(v -> v.split(",")[0])
                .orElse(request.getRemoteAddr());
    }
}
