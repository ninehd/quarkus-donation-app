package com.redhat.quarkus.donation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT); // pretty-print JSON

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
            System.err.println("JSON serialization error: " + e.getMessage());
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
            System.err.println("JSON deserialization error: " + e.getMessage());
            return null;
        }
    }
}
