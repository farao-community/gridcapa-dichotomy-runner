/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.app;

import com.farao_community.farao.data.crac_api.Crac;
import com.farao_community.farao.data.crac_io_api.CracImporters;
import com.farao_community.farao.data.rao_result_api.OptimizationState;
import com.farao_community.farao.data.rao_result_api.RaoResult;
import com.farao_community.farao.data.rao_result_json.RaoResultImporter;
import com.farao_community.farao.dichotomy.network.NetworkValidationResult;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInternalException;
import com.farao_community.farao.rao_runner.api.resource.RaoResponse;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class RaoRunnerResult implements NetworkValidationResult {
    private final RaoResponse raoResponse;
    private final boolean secure;

    RaoRunnerResult(RaoResponse raoResponse, UrlValidationService urlValidationService) {
        Crac crac = loadCrac(raoResponse, urlValidationService);
        RaoResult raoResult = loadRaoResult(raoResponse, crac, urlValidationService);
        this.raoResponse = raoResponse;
        secure = raoResultIsSecure(raoResult);
    }

    private static RaoResult loadRaoResult(RaoResponse raoResponse, Crac crac, UrlValidationService urlValidationService) {
        String raoResultUrlString = raoResponse.getRaoResultFileUrl();
        try (InputStream raoResultStream = urlValidationService.openUrlStream(raoResultUrlString)) {
            return new RaoResultImporter().importRaoResult(raoResultStream, crac);
        } catch (IOException exception) {
            throw new DichotomyInternalException(String.format("Cannot download RAO result file from URL '%s'", raoResultUrlString));
        }
    }

    private static Crac loadCrac(RaoResponse raoResponse, UrlValidationService urlValidationService) {
        String cracFileUrl = raoResponse.getCracFileUrl();
        try (InputStream cracResultStream = urlValidationService.openUrlStream(cracFileUrl)) {
            return CracImporters.importCrac(FilenameUtils.getName(URI.create(cracFileUrl).getPath()), cracResultStream);
        } catch (IOException exception) {
            throw new DichotomyInternalException(String.format("Cannot download CRAC result file from URL '%s'", cracFileUrl));
        }
    }

    private static boolean raoResultIsSecure(RaoResult raoResult) {
        return raoResult.getFunctionalCost(OptimizationState.AFTER_CRA) <= 0;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    public RaoResponse getRaoResponse() {
        return raoResponse;
    }
}
