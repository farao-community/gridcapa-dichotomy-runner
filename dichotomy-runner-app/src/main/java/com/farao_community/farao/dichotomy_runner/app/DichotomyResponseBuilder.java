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
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInternalException;
import com.farao_community.farao.dichotomy_runner.api.resource.*;
import com.farao_community.farao.rao_runner.api.resource.RaoResponse;
import org.apache.commons.io.FilenameUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public final class DichotomyResponseBuilder {

    private DichotomyResponseBuilder() {
        // Should not be instantiated
    }

    public static DichotomyResponse buildFromIndex(DichotomyRequest request, Index<NetworkValidationResultWrapper<RaoRunnerResult>> index) {
        // If one the steps are null it means that it stops due to index evaluation otherwise it could have continued.
        // If both are present, it is the expected case we just have to differentiate if the invalid step failed or if
        // it is just unsecure.
        LimitingCause limitingCause = LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION;
        String failureMessage = "None";
        if (index.lowestInvalidStep() != null && index.highestValidStep() != null) {
            if (index.lowestInvalidStep().isFailed()) {
                limitingCause = index.lowestInvalidStep().getReasonInvalid() == ReasonInvalid.GLSK_LIMITATION ?
                        LimitingCause.GLSK_LIMITATION : LimitingCause.COMPUTATION_FAILURE;
                failureMessage = index.lowestInvalidStep().getFailureMessage();
            } else {
                limitingCause = LimitingCause.CRITICAL_BRANCH;
            }
        }

        DichotomyStepResponse highestValidStepResponse = buildDichotomyStepResponse(index.highestValidStep());
        DichotomyStepResponse lowestInvalidStepResponse = buildDichotomyStepResponse(index.lowestInvalidStep());
        return new DichotomyResponse(request.getId(), highestValidStepResponse, lowestInvalidStepResponse, limitingCause, failureMessage);
    }

    private static DichotomyStepResponse buildDichotomyStepResponse(NetworkValidationResultWrapper<RaoRunnerResult> stepResult) {
        if (stepResult == null) {
            return null;
        }
        return stepResult.getNetworkValidationResult()
                .map(RaoRunnerResult::getRaoResponse)
                .map(raoResponse -> stepResponseFromRaoResponse(stepResult.stepValue(), raoResponse))
                .orElseGet(() -> stepResponseFromFailure(stepResult.stepValue()));
    }

    private static DichotomyStepResponse stepResponseFromRaoResponse(double stepValue, RaoResponse raoResponse) {
        return new DichotomyStepResponse(
                stepValue,
                convertToDichotomyFileResource(raoResponse.getNetworkWithPraFileUrl()),
                convertToDichotomyFileResource(raoResponse.getCracFileUrl()),
                convertToDichotomyFileResource(raoResponse.getRaoResultFileUrl()));
    }

    private static DichotomyStepResponse stepResponseFromFailure(double stepValue) {
        return new DichotomyStepResponse(stepValue, null, null, null);
    }

    private static DichotomyFileResource convertToDichotomyFileResource(String urlString) {
        try {
            URL url = new URL(urlString);
            return new DichotomyFileResource(FilenameUtils.getName(url.getPath()), urlString);
        } catch (MalformedURLException e) {
            throw new DichotomyInternalException(String.format("Cannot get filename from URL '%s'", urlString), e);
        }
    }
}
