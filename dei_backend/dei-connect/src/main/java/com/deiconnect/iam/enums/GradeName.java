package com.deiconnect.iam.enums;

public enum GradeName {
    ENTRY_LEVEL,
    ASSOCIATE,
    SENIOR_ASSOCIATE,
    SPECIALIST,
    MANAGER,
    SENIOR_MANAGER,
    DIRECTOR,
    VICE_PRESIDENT;

    public long getId() {
        return ordinal() + 1L;
    }

    public static GradeName fromId(Long id) {
        if (id == null) {
            return null;
        }
        GradeName[] values = values();
        int index = (int) (id - 1L);
        return (index >= 0 && index < values.length) ? values[index] : null;
    }
}
