/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.app;

import com.farao_community.farao.dichotomy.api.Index;
import com.farao_community.farao.dichotomy.network.NetworkValidationResultWrapper;
import com.farao_community.farao.dichotomy.network.ReasonInvalid;
import com.farao_community.farao.dichotomy_runner.api.resource.*;
import com.farao_community.farao.rao_runner.api.resource.RaoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class DichotomyResponseBuilderTest {

    private DichotomyRequest dichotomyRequest;
    private Index<NetworkValidationResultWrapper<RaoRunnerResult>> index;
    private RaoRunnerResult secureRaoRunnerResult;
    private RaoRunnerResult unsecureRaoRunnerResult;

    @BeforeEach
    void setUp() {
        RaoResponse raoResponse = new RaoResponse(
                "rao-response-id",
                "instant",
                "file:/rao-outputs/network-with-pra.xiidm",
                "file:/rao-outputs/crac.json",
                "file:/rao-outputs/rao-result.json");

        secureRaoRunnerResult = Mockito.mock(RaoRunnerResult.class);
        Mockito.when(secureRaoRunnerResult.getRaoResponse()).thenReturn(raoResponse);
        Mockito.when(secureRaoRunnerResult.isSecure()).thenReturn(true);

        unsecureRaoRunnerResult = Mockito.mock(RaoRunnerResult.class);
        Mockito.when(unsecureRaoRunnerResult.getRaoResponse()).thenReturn(raoResponse);
        Mockito.when(unsecureRaoRunnerResult.isSecure()).thenReturn(false);

        index = Mockito.mock(Index.class);

        dichotomyRequest = new DichotomyRequest(
                "request-id",
                new DichotomyFileResource("network_filename", "network_url"),
                new DichotomyFileResource("crac_filename", "crac_url"),
                new DichotomyFileResource("glsk_filename", "glsk_url"),
                new DichotomyFileResource("raoParameters_filename", "raoParameters_url"),
                new DichotomyParameters(0, 1000, 50, new SplittingFactorsConfiguration(Collections.emptyMap()))
        );
    }

    @Test
    void testWithSecureAndUnsecure() {
        NetworkValidationResultWrapper<RaoRunnerResult> lowestUnsecureStep = NetworkValidationResultWrapper.fromNetworkValidationResult(
                500,
                unsecureRaoRunnerResult
        );
        NetworkValidationResultWrapper<RaoRunnerResult> highestSecureStep = NetworkValidationResultWrapper.fromNetworkValidationResult(
                450,
                secureRaoRunnerResult
        );
        Mockito.when(index.lowestInvalidStep()).thenReturn(lowestUnsecureStep);
        Mockito.when(index.highestValidStep()).thenReturn(highestSecureStep);

        DichotomyResponse dichotomyResponse = DichotomyResponseBuilder.buildFromIndex(dichotomyRequest, index);

        assertEquals(450, dichotomyResponse.getHighestValidStep().getStepValue());
        assertEquals(500, dichotomyResponse.getLowestInvalidStep().getStepValue());
        assertEquals(LimitingCause.CRITICAL_BRANCH, dichotomyResponse.getLimitingCause());
        assertEquals("None", dichotomyResponse.getLimitingFailureMessage());
        assertTrue(dichotomyResponse.hasValidStep());
        assertEquals(450, dichotomyResponse.getHighestValidStepValue());
    }

    @Test
    void testWithSecureAndFailedWithGlskLimitation() {
        NetworkValidationResultWrapper<RaoRunnerResult> lowestUnsecureStep = NetworkValidationResultWrapper.fromNetworkValidationFailure(
                500,
                ReasonInvalid.GLSK_LIMITATION,
                "GLSK limits"
        );
        NetworkValidationResultWrapper<RaoRunnerResult> highestSecureStep = NetworkValidationResultWrapper.fromNetworkValidationResult(
                450,
                secureRaoRunnerResult
        );
        Mockito.when(index.lowestInvalidStep()).thenReturn(lowestUnsecureStep);
        Mockito.when(index.highestValidStep()).thenReturn(highestSecureStep);

        DichotomyResponse dichotomyResponse = DichotomyResponseBuilder.buildFromIndex(dichotomyRequest, index);

        assertEquals(450, dichotomyResponse.getHighestValidStep().getStepValue());
        assertEquals(500, dichotomyResponse.getLowestInvalidStep().getStepValue());
        assertEquals(LimitingCause.GLSK_LIMITATION, dichotomyResponse.getLimitingCause());
        assertEquals("GLSK limits", dichotomyResponse.getLimitingFailureMessage());
        assertTrue(dichotomyResponse.hasValidStep());
        assertEquals(450, dichotomyResponse.getHighestValidStepValue());
    }

    @Test
    void testWithSecureAndFailedWithValidationFailure() {
        NetworkValidationResultWrapper<RaoRunnerResult> lowestUnsecureStep = NetworkValidationResultWrapper.fromNetworkValidationFailure(
                500,
                ReasonInvalid.VALIDATION_FAILED,
                "RAO failed"
        );
        NetworkValidationResultWrapper<RaoRunnerResult> highestSecureStep = NetworkValidationResultWrapper.fromNetworkValidationResult(
                450,
                secureRaoRunnerResult
        );
        Mockito.when(index.lowestInvalidStep()).thenReturn(lowestUnsecureStep);
        Mockito.when(index.highestValidStep()).thenReturn(highestSecureStep);

        DichotomyResponse dichotomyResponse = DichotomyResponseBuilder.buildFromIndex(dichotomyRequest, index);

        assertEquals(450, dichotomyResponse.getHighestValidStep().getStepValue());
        assertEquals(500, dichotomyResponse.getLowestInvalidStep().getStepValue());
        assertEquals(LimitingCause.COMPUTATION_FAILURE, dichotomyResponse.getLimitingCause());
        assertEquals("RAO failed", dichotomyResponse.getLimitingFailureMessage());
        assertTrue(dichotomyResponse.hasValidStep());
        assertEquals(450, dichotomyResponse.getHighestValidStepValue());
    }

    @Test
    void testWithOnlyOneUnsecureStep() {
        NetworkValidationResultWrapper<RaoRunnerResult> lowestUnsecureStep = NetworkValidationResultWrapper.fromNetworkValidationResult(
                500,
                unsecureRaoRunnerResult
        );
        Mockito.when(index.lowestInvalidStep()).thenReturn(lowestUnsecureStep);
        Mockito.when(index.highestValidStep()).thenReturn(null);

        DichotomyResponse dichotomyResponse = DichotomyResponseBuilder.buildFromIndex(dichotomyRequest, index);

        assertEquals(500, dichotomyResponse.getLowestInvalidStep().getStepValue());
        assertEquals(LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION, dichotomyResponse.getLimitingCause());
        assertFalse(dichotomyResponse.hasValidStep());
    }

    @Test
    void testWithOnlyOneFailedStep() {
        NetworkValidationResultWrapper<RaoRunnerResult> lowestUnsecureStep = NetworkValidationResultWrapper.fromNetworkValidationFailure(
                500,
                ReasonInvalid.VALIDATION_FAILED,
                "RAO failed"
        );
        Mockito.when(index.lowestInvalidStep()).thenReturn(lowestUnsecureStep);
        Mockito.when(index.highestValidStep()).thenReturn(null);

        DichotomyResponse dichotomyResponse = DichotomyResponseBuilder.buildFromIndex(dichotomyRequest, index);

        assertEquals(500, dichotomyResponse.getLowestInvalidStep().getStepValue());
        assertEquals(LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION, dichotomyResponse.getLimitingCause());
        assertFalse(dichotomyResponse.hasValidStep());
    }

    @Test
    void testWithOnlyOneSecureStep() {
        NetworkValidationResultWrapper<RaoRunnerResult> highestSecureStep = NetworkValidationResultWrapper.fromNetworkValidationResult(
                450,
                secureRaoRunnerResult
        );
        Mockito.when(index.lowestInvalidStep()).thenReturn(null);
        Mockito.when(index.highestValidStep()).thenReturn(highestSecureStep);

        DichotomyResponse dichotomyResponse = DichotomyResponseBuilder.buildFromIndex(dichotomyRequest, index);

        assertEquals(450, dichotomyResponse.getHighestValidStep().getStepValue());
        assertEquals(LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION, dichotomyResponse.getLimitingCause());
        assertTrue(dichotomyResponse.hasValidStep());
    }

    @Test
    void testWithEmptyIndex() {
        Mockito.when(index.lowestInvalidStep()).thenReturn(null);
        Mockito.when(index.highestValidStep()).thenReturn(null);

        DichotomyResponse dichotomyResponse = DichotomyResponseBuilder.buildFromIndex(dichotomyRequest, index);

        assertEquals(LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION, dichotomyResponse.getLimitingCause());
        assertFalse(dichotomyResponse.hasValidStep());
    }
}
