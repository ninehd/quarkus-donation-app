package com.redhat.quarkus.donation.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.io.IOException;
import java.util.Map;

@ApplicationScoped
public class GlobalExceptionHandler {

    private static final Logger log = Logger.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles WebApplicationException
     */
    @ServerExceptionMapper
    public RestResponse<Map<String, String>> handleWebApplicationException(WebApplicationException e) {
        log.errorf("External call error: %s", e.getMessage());
        int statusCode = (e.getResponse() != null) ? e.getResponse().getStatus() : 500;

        Response.Status status = Response.Status.fromStatusCode(statusCode);
        if (status == null) {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return RestResponse.status(status, Map.of(
                "message", "External call error: " + e.getMessage()
        ));
    }

    /**
     * Handles IllegalArgumentException errors
     */
    @ServerExceptionMapper
    public RestResponse<Map<String, String>> handleInvalidArgument(IllegalArgumentException e) {
        log.errorf("Bad request: %s", e.getMessage());
        return RestResponse.status(Response.Status.BAD_REQUEST,
                Map.of("message", e.getMessage() != null ? e.getMessage() : "Invalid request"));
    }

    /**
     * Handles all other uncaught exceptions
     */
    @ServerExceptionMapper
    public RestResponse<Map<String, String>> handleGeneric(Exception e) {
        log.error("Unexpected error", e);
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR,
                Map.of("message", "An unexpected error occurred"));
    }
}