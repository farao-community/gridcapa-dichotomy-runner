/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api.resource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class DichotomyResponseTest {
    private static final double EPSILON = 0.3;

    @Test
    void checkDichotomyResponseNormalUsage() {
        DichotomyFileResource networkWithPra = new DichotomyFileResource("networkWithPra.txt", "http://path/to/network-with-pra/file");
        DichotomyFileResource cracResult = new DichotomyFileResource("cracResult.txt", "http://path/to/crac-result/file");
        DichotomyFileResource raoResult = new DichotomyFileResource("raoResult.txt", "http://path/to/rao-result/file");
        DichotomyStepResponse higherSecureStep = new DichotomyStepResponse(200., networkWithPra, cracResult, raoResult);
        DichotomyStepResponse lowerUnsecureStep = new DichotomyStepResponse(250., networkWithPra, cracResult, raoResult);
        DichotomyResponse dichotomyResponse = new DichotomyResponse("id", higherSecureStep, lowerUnsecureStep, null, null);
        assertNotNull(dichotomyResponse);
        assertEquals("id", dichotomyResponse.getId());
        assertEquals(200., dichotomyResponse.getHighestValidStep().getStepValue(), EPSILON);
        assertEquals("networkWithPra.txt", dichotomyResponse.getHighestValidStep().getNetworkWithPra().getFilename());
        assertEquals("http://path/to/network-with-pra/file", dichotomyResponse.getHighestValidStep().getNetworkWithPra().getUrl());
        assertEquals("cracResult.txt", dichotomyResponse.getHighestValidStep().getCracResult().getFilename());
        assertEquals("http://path/to/crac-result/file", dichotomyResponse.getHighestValidStep().getCracResult().getUrl());
        assertEquals("raoResult.txt", dichotomyResponse.getHighestValidStep().getRaoResult().getFilename());
        assertEquals("http://path/to/rao-result/file", dichotomyResponse.getHighestValidStep().getRaoResult().getUrl());
        assertEquals(250., dichotomyResponse.getLowestInvalidStep().getStepValue(), EPSILON);
        assertEquals("networkWithPra.txt", dichotomyResponse.getLowestInvalidStep().getNetworkWithPra().getFilename());
        assertEquals("http://path/to/network-with-pra/file", dichotomyResponse.getLowestInvalidStep().getNetworkWithPra().getUrl());
        assertEquals("cracResult.txt", dichotomyResponse.getLowestInvalidStep().getCracResult().getFilename());
        assertEquals("http://path/to/crac-result/file", dichotomyResponse.getLowestInvalidStep().getCracResult().getUrl());
        assertEquals("raoResult.txt", dichotomyResponse.getLowestInvalidStep().getRaoResult().getFilename());
        assertEquals("http://path/to/rao-result/file", dichotomyResponse.getLowestInvalidStep().getRaoResult().getUrl());
    }

}
