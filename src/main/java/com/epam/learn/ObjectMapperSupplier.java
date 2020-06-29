package com.epam.learn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

public class ObjectMapperSupplier {

    @Getter
    private static final ObjectMapperSupplier instance = new ObjectMapperSupplier();

    @Getter
    private final ObjectMapper mapper;

    private ObjectMapperSupplier() {
        mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
    }
}
