package com.epam.learn.model;

import lombok.Getter;

public enum TaskStatus {
    OPEN("Open"), COMPLETED("Completed"), RE_OPEN("Re open"), CLOSED("Closed");

    @Getter
    private String name;

    TaskStatus(String name) {
        this.name = name;
    }
}
