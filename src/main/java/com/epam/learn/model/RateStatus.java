package com.epam.learn.model;

import java.util.HashMap;
import java.util.Map;

public enum RateStatus {
    APPROVE, REJECT;

    private static final Map<String, RateStatus> nameIndex = new HashMap<>();
    static {
        for (RateStatus status : RateStatus.values()) {
            nameIndex.put(status.name(), status);
        }
    }
    public static RateStatus getByName(String name) {
        return nameIndex.get(name);
    }
}
