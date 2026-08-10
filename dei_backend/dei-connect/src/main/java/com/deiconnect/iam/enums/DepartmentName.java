package com.deiconnect.iam.enums;

public enum DepartmentName {
    INFRASTRUCTURE,
    CYBERSECURITY,
    HR,
    SOFTWARE_ENGINEERING,
    END_USER_SUPPORT;

    public long getId() {
        return ordinal() + 1L;
    }

    public static DepartmentName fromId(Long id) {
        if (id == null) {
            return null;
        }
        DepartmentName[] values = values();
        int index = (int) (id - 1L);
        return (index >= 0 && index < values.length) ? values[index] : null;
    }
}
