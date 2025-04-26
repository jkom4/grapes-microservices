package grapes.microservices.paymentbackend.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for parsing and handling data formats.
 * Provides methods to parse query string-like data formats.
 */
public class DataUtils {
    /**
     * Parses data in a string with the format "key1=value1&key2=value2&...".
     * Used for processing URL query parameters or similar formatted data.
     *
     * @param data The string to parse
     * @return A map containing the key-value pairs
     */
    public static Map<String, String> parseData(String data) {
        Map<String, String> dataMap = new HashMap<>();
        String[] pairs = data.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);  // Split by '=' (max 2 parts)
            if (keyValue.length == 2) {
                dataMap.put(keyValue[0], keyValue[1]);
            } else {
                System.out.println("[WARNING] Malformed pair: " + pair);
            }
        }
        return dataMap;
    }
}