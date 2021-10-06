/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.app;

import com.farao_community.farao.dichotomy.network.NetworkValidationException;
import com.farao_community.farao.dichotomy.network.NetworkValidator;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyFileResource;
import com.farao_community.farao.rao_runner.api.resource.RaoRequest;
import com.farao_community.farao.rao_runner.api.resource.RaoResponse;
import com.farao_community.farao.rao_runner.starter.RaoRunnerClient;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.network.Network;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class RaoRunnerValidator implements NetworkValidator<RaoRunnerResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RaoRunnerValidator.class);

    private final String requestId;
    private final String cracUrl;
    private final String raoParametersUrl;
    private final MinioAdapter minioAdapter;
    private final RaoRunnerClient raoRunnerClient;
    private final UrlValidationService urlValidationService;

    public RaoRunnerValidator(String requestId,
                              String cracUrl,
                              String raoParametersUrl,
                              MinioAdapter minioAdapter,
                              RaoRunnerClient raoRunnerClient,
                              UrlValidationService urlValidationService) {
        this.requestId = requestId;
        this.cracUrl = cracUrl;
        this.raoParametersUrl = raoParametersUrl;
        this.minioAdapter = minioAdapter;
        this.raoRunnerClient = raoRunnerClient;
        this.urlValidationService = urlValidationService;
    }

    @Override
    public RaoRunnerResult validateNetwork(Network network) throws NetworkValidationException {
        DichotomyFileResource networkFile = saveNetwork(network);
        RaoRequest raoRequest = buildRaoRequest(networkFile);
        try {
            LOGGER.info("RAO request sent: {}", raoRequest);
            RaoResponse raoResponse = raoRunnerClient.runRao(raoRequest);
            LOGGER.info("RAO response received: {}", raoResponse);
            return new RaoRunnerResult(raoResponse, urlValidationService);
        } catch (RuntimeException e) {
            throw new NetworkValidationException("RAO run failed", e);
        }
    }

    private RaoRequest buildRaoRequest(DichotomyFileResource networkFile) {
        String raoRequestId = String.format("%s-%s", requestId, networkFile.getFilename());
        return new RaoRequest(raoRequestId, networkFile.getUrl(), cracUrl, raoParametersUrl);
    }

    private DichotomyFileResource saveNetwork(Network network) throws NetworkValidationException {
        try {
            String networkFilename = networkScaledFilename(network);
            String networkFilePath = String.format("%s/%s", requestId, networkFilename);
            MemDataSource memDataSource = new MemDataSource();
            Exporters.export("XIIDM", network, new Properties(), memDataSource);
            minioAdapter.putFile(IOUtils.toByteArray(memDataSource.newInputStream("", "xiidm")), networkFilePath);
            return minioAdapter.generateFileResource(networkFilePath);
        } catch (IOException e) {
            throw new NetworkValidationException("Could not save scaled network for value", e);
        }
    }

    private String networkScaledFilename(Network network) {
        String variantName = network.getVariantManager().getWorkingVariantId();
        return String.format("%s-%s.xiidm", network.getNameOrId(), variantName);
    }
}
