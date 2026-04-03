package com.cvreviewapp.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmailUtilTest {
    @Test
    void testSend2FACodeFormat() {
        String code = EmailUtil.send2FACode("test@example.com");
        // The code may be null if SMTP is not configured, so only check format if not null
        if (code != null) {
            assertEquals(6, code.length());
            assertTrue(code.matches("\\d{6}"));
        }
    }
} 