package com.deiconnect.erg.client;

import com.deiconnect.common.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserClientFallbackTest {

    private final UserClientFallback fallback = new UserClientFallback();

    @Test
    void getByIdInternal_ReturnsOfflineFallbackUser() {
        UserResponse response = fallback.getByIdInternal(99L);
        assertNotNull(response);
        assertEquals(99L, response.userId());
        assertEquals("OFFLINE", response.employeeId());
        assertEquals(Role.EMPLOYEE, response.role());
    }

    @Test
    void getByIdsInternal_ReturnsEmptyList() {
        List<UserResponse> list = fallback.getByIdsInternal(List.of(1L, 2L));
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }
}
