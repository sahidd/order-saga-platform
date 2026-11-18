package com.sahidanwar.platform;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class SagaJson {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private SagaJson() {
    }

    public static String write(SagaEvent event) {
        try {
            return MAPPER.writeValueAsString(event);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not write saga event.", exception);
        }
    }

    public static SagaEvent read(String json) {
        try {
            return MAPPER.readValue(json, SagaEvent.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Could not read saga event.", exception);
        }
    }
}
