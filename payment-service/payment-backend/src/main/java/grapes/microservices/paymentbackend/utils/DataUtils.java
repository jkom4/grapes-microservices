package grapes.microservices.paymentbackend.utils;

import java.util.HashMap;
import java.util.Map;

public class DataUtils {
    /**
     * Parse data in a string with the format "key1=value1&key2=value2&..."
     * @param data The brut string to parse
     * @return A map containing the key-value pairs
     */
    public static Map<String, String> parseData(String data) {
        Map<String, String> dataMap = new HashMap<>();
        String[] pairs = data.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);  // Diviser par '=' (max 2 parties)
            if (keyValue.length == 2) {
                dataMap.put(keyValue[0], keyValue[1]);
            } else {
                System.out.println("[WARNING] Malformed pair: " + pair);
            }
        }
        return dataMap;
    }
}