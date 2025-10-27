package com.redhat.quarkus.donation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jboss.logging.Logger;

public class JsonUtils {

    private static final Logger log = Logger.getLogger(JsonUtils.class);
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Serialize a Java object to a JSON string
     *
     * @param object The object to convert
     * @return A JSON string, or null if an error occurs
     */
    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON serialization error", e);
            return null;
        }
    }

    /**
     * Deserialize a JSON string into a Java object
     *
     * @param json  The JSON string
     * @param clazz The target class
     * @param <T>   The type of the returned object
     * @return An instance of T, or null if an error occurs
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON deserialization error", e);
            return null;
        }
    }
}
