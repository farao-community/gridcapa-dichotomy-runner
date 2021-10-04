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
class CseIdccShiftDispatcherConfigurationTest {
    private static final double DOUBLE_TOLERANCE = 0.001;

    @Test
    void checkNormal() {
        Map<String, Double> splittingFactors = new TreeMap<>();
        splittingFactors.put("10YFR-RTE------C", 0.3);
        splittingFactors.put("10YCH-SWISSGRIDZ", 0.3);
        splittingFactors.put("10YAT-APG------L", 0.2);
        splittingFactors.put("10YSI-ELES-----O", 0.2);
        splittingFactors.put("10YIT-GRTN-----B", -1.);

        Map<String, Double> referenceExchanges = new TreeMap<>();
        referenceExchanges.put("10YFR-RTE------C", 3000.);
        referenceExchanges.put("10YCH-SWISSGRIDZ", 1500.);
        referenceExchanges.put("10YAT-APG------L", 400.);
        referenceExchanges.put("10YSI-ELES-----O", 1200.);

        Map<String, Double> ntcs2 = new TreeMap<>();
        ntcs2.put("10YFR-RTE------C", 4000.);
        ntcs2.put("10YCH-SWISSGRIDZ", 3700.);
        ntcs2.put("10YAT-APG------L", 1250.);
        ntcs2.put("10YSI-ELES-----O", 1500.);

        CseIdccShiftDispatcherConfiguration configuration = new CseIdccShiftDispatcherConfiguration(splittingFactors, referenceExchanges, ntcs2);
        assertEquals(0.3, configuration.getSplittingFactors().get("10YFR-RTE------C"), DOUBLE_TOLERANCE);
        assertEquals(0.3, configuration.getSplittingFactors().get("10YCH-SWISSGRIDZ"), DOUBLE_TOLERANCE);
        assertEquals(0.2, configuration.getSplittingFactors().get("10YAT-APG------L"), DOUBLE_TOLERANCE);
        assertEquals(0.2, configuration.getSplittingFactors().get("10YSI-ELES-----O"), DOUBLE_TOLERANCE);
        assertEquals(-1.0, configuration.getSplittingFactors().get("10YIT-GRTN-----B"), DOUBLE_TOLERANCE);

        assertEquals(3000., configuration.getReferenceExchanges().get("10YFR-RTE------C"), DOUBLE_TOLERANCE);
        assertEquals(1500., configuration.getReferenceExchanges().get("10YCH-SWISSGRIDZ"), DOUBLE_TOLERANCE);
        assertEquals(400., configuration.getReferenceExchanges().get("10YAT-APG------L"), DOUBLE_TOLERANCE);
        assertEquals(1200., configuration.getReferenceExchanges().get("10YSI-ELES-----O"), DOUBLE_TOLERANCE);

        assertEquals(4000., configuration.getNtcs2().get("10YFR-RTE------C"), DOUBLE_TOLERANCE);
        assertEquals(3700., configuration.getNtcs2().get("10YCH-SWISSGRIDZ"), DOUBLE_TOLERANCE);
        assertEquals(1250., configuration.getNtcs2().get("10YAT-APG------L"), DOUBLE_TOLERANCE);
        assertEquals(1500., configuration.getNtcs2().get("10YSI-ELES-----O"), DOUBLE_TOLERANCE);
    }

    @Test
    void checkInvalidCountriesForSplittingFactors() {
        Map<String, Double> splittingFactors = new TreeMap<>();
        splittingFactors.put("fake-country", 0.3);
        splittingFactors.put("10YCH-SWISSGRIDZ", 0.3);
        splittingFactors.put("10YAT-APG------L", 0.2);
        splittingFactors.put("10YSI-ELES-----O", 0.2);
        splittingFactors.put("10YIT-GRTN-----B", -1.);

        Map<String, Double> referenceExchanges = new TreeMap<>();
        referenceExchanges.put("10YFR-RTE------C", 3000.);
        referenceExchanges.put("10YCH-SWISSGRIDZ", 1500.);
        referenceExchanges.put("10YAT-APG------L", 400.);
        referenceExchanges.put("10YSI-ELES-----O", 1200.);

        Map<String, Double> ntcs2 = new TreeMap<>();
        ntcs2.put("10YFR-RTE------C", 4000.);
        ntcs2.put("10YCH-SWISSGRIDZ", 3700.);
        ntcs2.put("10YAT-APG------L", 1250.);
        ntcs2.put("10YSI-ELES-----O", 1500.);

        assertThrows(DichotomyInvalidDataException.class, () -> new CseIdccShiftDispatcherConfiguration(splittingFactors, referenceExchanges, ntcs2));
    }

    @Test
    void checkInvalidCountriesForReferenceExchanges() {
        Map<String, Double> splittingFactors = new TreeMap<>();
        splittingFactors.put("10YFR-RTE------C", 0.3);
        splittingFactors.put("10YCH-SWISSGRIDZ", 0.3);
        splittingFactors.put("10YAT-APG------L", 0.2);
        splittingFactors.put("10YSI-ELES-----O", 0.2);
        splittingFactors.put("10YIT-GRTN-----B", -1.);

        Map<String, Double> referenceExchanges = new TreeMap<>();
        referenceExchanges.put("10YFR-RTE------C", 3000.);
        referenceExchanges.put("fake-country", 1500.);
        referenceExchanges.put("10YAT-APG------L", 400.);
        referenceExchanges.put("10YSI-ELES-----O", 1200.);

        Map<String, Double> ntcs2 = new TreeMap<>();
        ntcs2.put("10YFR-RTE------C", 4000.);
        ntcs2.put("10YCH-SWISSGRIDZ", 3700.);
        ntcs2.put("10YAT-APG------L", 1250.);
        ntcs2.put("10YSI-ELES-----O", 1500.);

        assertThrows(DichotomyInvalidDataException.class, () -> new CseIdccShiftDispatcherConfiguration(splittingFactors, referenceExchanges, ntcs2));
    }

    @Test
    void checkInvalidCountriesForNtcs2() {
        Map<String, Double> splittingFactors = new TreeMap<>();
        splittingFactors.put("10YFR-RTE------C", 0.3);
        splittingFactors.put("10YCH-SWISSGRIDZ", 0.3);
        splittingFactors.put("10YAT-APG------L", 0.2);
        splittingFactors.put("10YSI-ELES-----O", 0.2);
        splittingFactors.put("10YIT-GRTN-----B", -1.);

        Map<String, Double> referenceExchanges = new TreeMap<>();
        referenceExchanges.put("10YFR-RTE------C", 3000.);
        referenceExchanges.put("10YCH-SWISSGRIDZ", 1500.);
        referenceExchanges.put("10YAT-APG------L", 400.);
        referenceExchanges.put("10YSI-ELES-----O", 1200.);

        Map<String, Double> ntcs2 = new TreeMap<>();
        ntcs2.put("10YFR-RTE------C", 4000.);
        ntcs2.put("10YCH-SWISSGRIDZ", 3700.);
        ntcs2.put("fake-country", 1250.);
        ntcs2.put("10YSI-ELES-----O", 1500.);

        assertThrows(DichotomyInvalidDataException.class, () -> new CseIdccShiftDispatcherConfiguration(splittingFactors, referenceExchanges, ntcs2));
    }

    @Test
    void checkInvalidWithCountryCode() {
        Map<String, Double> splittingFactors = new TreeMap<>();
        splittingFactors.put("FR", 0.3);
        splittingFactors.put("CH", 0.3);
        splittingFactors.put("AT", 0.2);
        splittingFactors.put("SI", 0.2);
        splittingFactors.put("IT", -1.);

        Map<String, Double> referenceExchanges = new TreeMap<>();
        referenceExchanges.put("FR", 3000.);
        referenceExchanges.put("CH", 1500.);
        referenceExchanges.put("AT", 400.);
        referenceExchanges.put("SI", 1200.);

        Map<String, Double> ntcs2 = new TreeMap<>();
        ntcs2.put("FR", 4000.);
        ntcs2.put("CH", 3700.);
        ntcs2.put("AT", 1250.);
        ntcs2.put("SI", 1500.);

        assertThrows(DichotomyInvalidDataException.class, () -> new CseIdccShiftDispatcherConfiguration(splittingFactors, referenceExchanges, ntcs2));
    }
}
