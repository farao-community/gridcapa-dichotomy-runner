/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class DichotomyInternalExceptionTest {
    @Test
    void checkStandardExceptionUsage() {
        AbstractDichotomyException exception = new DichotomyInternalException("Exception message");
        assertEquals("Exception message", exception.getMessage());
        assertNull(exception.getCause());
        assertEquals(500, exception.getStatus());
        assertEquals("500-InternalException", exception.getCode());
        assertEquals("Exception message", exception.getTitle());
        assertEquals("Exception message", exception.getDetails());

        Exception cause = new RuntimeException("Cause");
        exception = new DichotomyInternalException("Exception message", cause);
        assertEquals("Exception message", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(500, exception.getStatus());
        assertEquals("500-InternalException", exception.getCode());
        assertEquals("Exception message", exception.getTitle());
        assertEquals("Exception message; nested exception is java.lang.RuntimeException: Cause", exception.getDetails());
    }

}
