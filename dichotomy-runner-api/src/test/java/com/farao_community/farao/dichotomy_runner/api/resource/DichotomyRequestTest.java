/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api.resource;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class DichotomyRequestTest {
    private static final double EPSILON = 0.3;

    @Test
    void checkDichotomyStartNormalUsage() {
        DichotomyFileResource network = new DichotomyFileResource("network.txt", "http://path/to/network/file");
        DichotomyFileResource crac = new DichotomyFileResource("crac.txt", "http://path/to/crac/file");
        DichotomyFileResource glsk = new DichotomyFileResource("glsk.txt", "http://path/to/glsk/file");
        Map<String, Double> splittingFactors = new TreeMap<>();
        splittingFactors.put("FR", 0.4);
        splittingFactors.put("AT", 0.4);
        splittingFactors.put("SI", 0.2);
        splittingFactors.put("IT", -1.);
        DichotomyParameters parameters = new DichotomyParameters(0., 1000., 50., new SplittingFactorsConfiguration(splittingFactors));
        DichotomyRequest dichotomyRequest = new DichotomyRequest("id", network, crac, glsk, parameters);
        assertNotNull(dichotomyRequest);
        assertEquals("id", dichotomyRequest.getId());
        assertTrue(dichotomyRequest.getParameters().getShiftDispatcherConfiguration() instanceof SplittingFactorsConfiguration);
        SplittingFactorsConfiguration splittingFactorsConfiguration = (SplittingFactorsConfiguration) dichotomyRequest.getParameters().getShiftDispatcherConfiguration();
        assertEquals(0.4, splittingFactorsConfiguration.getSplittingFactors().get("FR"), EPSILON);
        assertEquals(0.4, splittingFactorsConfiguration.getSplittingFactors().get("AT"), EPSILON);
        assertEquals(0.2, splittingFactorsConfiguration.getSplittingFactors().get("SI"), EPSILON);
        assertEquals(-1., splittingFactorsConfiguration.getSplittingFactors().get("IT"), EPSILON);
        assertEquals(0., dichotomyRequest.getParameters().getMinValue(), EPSILON);
        assertEquals(1000., dichotomyRequest.getParameters().getMaxValue(), EPSILON);
        assertEquals(50., dichotomyRequest.getParameters().getPrecision(), EPSILON);
    }
}
