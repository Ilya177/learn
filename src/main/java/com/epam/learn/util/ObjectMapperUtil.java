package com.epam.learn.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ObjectMapperUtil {

    private ObjectMapperUtil() {
    }

    public static ByteBuffer toByteBuffer(ObjectMapper mapper, Object object) {
        String json = toString(mapper, object);
        return ByteBuffer.wrap(json.getBytes());
    }

    public static <T> Optional<T> toObject(ObjectMapper mapper, ByteBuffer buffer, Class<T> type) {
        return Optional.ofNullable(buffer)
                .map(ByteBuffer::array)
                .map(String::new)
                .map(json -> toObject(mapper, json, type));
    }

    public static <T> T toObject(ObjectMapper mapper, String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            log.warn("Cannot convert json to object");
            return null;
        }
    }

    public static String toString(ObjectMapper mapper, Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Cannot convert object to json");
            return "";
        }
    }

    public static String getValue(JsonNode node, String fieldName) {
        if (node.get(fieldName) != null) {
            return node.get(fieldName).asText();
        }
        return null;
    }
}
