/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api.resource;

import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInvalidDataException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class SplittingFactorsConfigurationTest {
    private static final double DOUBLE_TOLERANCE = 0.001;

    @Test
    void checkNormal() {
        Map<String, Double> splittingFactors = new TreeMap<>();
        splittingFactors.put("FR", 0.8);
        splittingFactors.put("AT", 0.2);
        splittingFactors.put("IT", -1.);
        SplittingFactorsConfiguration splittingFactorsConfiguration = new SplittingFactorsConfiguration(splittingFactors);
        assertEquals(0.8, splittingFactorsConfiguration.getSplittingFactors().get("FR"), DOUBLE_TOLERANCE);
        assertEquals(0.2, splittingFactorsConfiguration.getSplittingFactors().get("AT"), DOUBLE_TOLERANCE);
        assertEquals(-1.0, splittingFactorsConfiguration.getSplittingFactors().get("IT"), DOUBLE_TOLERANCE);
    }

    @Test
    void checkConstructionFailsWithSplittingFactorsSumDifferentThanZero() {
        Map<String, Double> splittingFactors = new TreeMap<>();
        splittingFactors.put("FR", 0.8);
        splittingFactors.put("IT", -1.);
        assertThrows(DichotomyInvalidDataException.class, () -> new SplittingFactorsConfiguration(splittingFactors));
    }
}
