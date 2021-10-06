/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.app;

import com.farao_community.farao.dichotomy.api.Index;
import com.farao_community.farao.dichotomy.api.ValidationException;
import com.farao_community.farao.dichotomy.network.NetworkValidationResultWrapper;
import com.farao_community.farao.dichotomy.network.ReasonUnsecure;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInternalException;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyFileResource;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyRequest;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyResponse;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyStepResponse;
import com.farao_community.farao.rao_runner.api.resource.RaoResponse;
import org.apache.commons.io.FilenameUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public final class DichotomyResponseBuilder {

    private DichotomyResponseBuilder() {
        // Should not be instantiated
    }

    public static DichotomyResponse buildDichotomyResponse(DichotomyRequest request, Index<NetworkValidationResultWrapper<RaoRunnerResult>> index) {
        return new DichotomyResponse(
                request.getId(),
                buildDichotomyStepResponse(index.higherSecureStep()),
                buildDichotomyStepResponse(index.lowerUnsecureStep()),
                buildReasonUnsecureForResponse(index.lowerUnsecureStep()));
    }

    public static DichotomyResponse fromValidationException(DichotomyRequest request,
                                                            Index<NetworkValidationResultWrapper<RaoRunnerResult>> index,
                                                            ValidationException e) {
        return new DichotomyResponse(
                request.getId(),
                buildDichotomyStepResponse(index.higherSecureStep()),
                buildDichotomyStepResponse(index.lowerUnsecureStep()),
                buildReasonUnsecureForResponse(index.lowerUnsecureStep()),
                e.getMessage());
    }

    private static ReasonUnsecure buildReasonUnsecureForResponse(NetworkValidationResultWrapper<?> lowerUnsecureStep) {
        return Optional.ofNullable(lowerUnsecureStep)
                .map(NetworkValidationResultWrapper::getReasonUnsecure)
                .orElse(null);
    }

    private static DichotomyStepResponse buildDichotomyStepResponse(NetworkValidationResultWrapper<RaoRunnerResult> stepResult) {
        return Optional.ofNullable(stepResult)
                .flatMap(NetworkValidationResultWrapper::getNetworkValidationResult)
                .map(RaoRunnerResult::getRaoResponse)
                .map(raoResponse -> stepResponseFromRaoResponse(stepResult.stepValue(), raoResponse))
                .orElse(null);
    }

    private static DichotomyStepResponse stepResponseFromRaoResponse(double stepValue, RaoResponse raoResponse) {
        return new DichotomyStepResponse(
                stepValue,
                convertToDichotomyFileResource(raoResponse.getNetworkWithPraFileUrl()),
                convertToDichotomyFileResource(raoResponse.getCracFileUrl()),
                convertToDichotomyFileResource(raoResponse.getRaoResultFileUrl()));
    }

    private static DichotomyFileResource convertToDichotomyFileResource(String urlString) {
        if (urlString == null) {
            return null;
        }
        try {
            URL url = new URL(urlString);
            return new DichotomyFileResource(FilenameUtils.getName(url.getPath()), urlString);
        } catch (MalformedURLException e) {
            throw new DichotomyInternalException(String.format("Cannot get filename from URL '%s'", urlString), e);
        }
    }
}
