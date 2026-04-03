package com.cvreviewapp.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HashUtilTest {
    @Test
    void testHashAndVerifyPassword() {
        String password = "mySecret123";
        String hash = HashUtil.hashPassword(password);
        assertNotNull(hash);
        assertTrue(HashUtil.verifyPassword(password, hash));
        assertFalse(HashUtil.verifyPassword("wrongPassword", hash));
    }
} 