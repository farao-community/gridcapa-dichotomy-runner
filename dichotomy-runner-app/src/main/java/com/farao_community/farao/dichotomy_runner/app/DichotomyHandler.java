/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.app;

import com.farao_community.farao.commons.ZonalData;
import com.farao_community.farao.data.glsk.api.io.GlskDocumentImporters;
import com.farao_community.farao.dichotomy.api.*;
import com.farao_community.farao.dichotomy.network.NetworkValidationResultWrapper;
import com.farao_community.farao.dichotomy.network.NetworkValidator;
import com.farao_community.farao.dichotomy.network.scaling.CseIdccShiftDispatcher;
import com.farao_community.farao.dichotomy.network.scaling.ScalingNetworkValidationStrategy;
import com.farao_community.farao.dichotomy.network.scaling.ShiftDispatcher;
import com.farao_community.farao.dichotomy.network.scaling.SplittingFactors;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInternalException;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInvalidDataException;
import com.farao_community.farao.dichotomy_runner.api.resource.*;
import com.farao_community.farao.rao_runner.starter.RaoRunnerClient;
import com.powsybl.action.util.Scalable;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Component
public class DichotomyHandler {
    private final MinioAdapter minioAdapter;
    private final RaoRunnerClient raoRunnerClient;
    private final UrlValidationService urlValidationService;

    public DichotomyHandler(MinioAdapter minioAdapter, RaoRunnerClient raoRunnerClient, UrlValidationService urlValidationService) {
        this.minioAdapter = minioAdapter;
        this.raoRunnerClient = raoRunnerClient;
        this.urlValidationService = urlValidationService;
    }

    public DichotomyResponse handleDichotomyRequest(DichotomyRequest request) {
        Index<NetworkValidationResultWrapper<RaoRunnerResult>> index = buildIndex(request.getParameters());
        DichotomyEngine<NetworkValidationResultWrapper<RaoRunnerResult>> engine = new DichotomyEngine<>(
                index,
                buildIndexStrategy(request.getParameters().getIndexStrategyConfiguration()),
                buildValidationStrategy(request));
        engine.run();
        return DichotomyResponseBuilder.buildFromIndex(request, index);
    }

    private static Index<NetworkValidationResultWrapper<RaoRunnerResult>> buildIndex(DichotomyParameters parameters) {
        return new Index<>(parameters.getMinValue(), parameters.getMaxValue(), parameters.getPrecision());
    }

    private ValidationStrategy<NetworkValidationResultWrapper<RaoRunnerResult>> buildValidationStrategy(DichotomyRequest request) {
        Network network = loadNetwork(request.getNetwork());
        NetworkValidator<RaoRunnerResult> networkValidator = new RaoRunnerValidator(
                request.getId(),
                request.getCrac().getUrl(),
                request.getRaoParameters().getUrl(),
                minioAdapter,
                raoRunnerClient,
                urlValidationService);

        return new ScalingNetworkValidationStrategy<>(
                network,
                networkValidator,
                loadGlsk(request.getGlsk(), network),
                buildShiftDispatcher(request.getParameters().getShiftDispatcherConfiguration()));
    }

    static ShiftDispatcher buildShiftDispatcher(ShiftDispatcherConfiguration shiftDispatcherConfiguration) {
        if (shiftDispatcherConfiguration instanceof CseIdccShiftDispatcherConfiguration) {
            CseIdccShiftDispatcherConfiguration configuration = (CseIdccShiftDispatcherConfiguration) shiftDispatcherConfiguration;
            return getCseIdccDispatcher(configuration);
        } else if (shiftDispatcherConfiguration instanceof SplittingFactorsConfiguration) {
            SplittingFactorsConfiguration configuration = (SplittingFactorsConfiguration) shiftDispatcherConfiguration;
            return new SplittingFactors(configuration.getSplittingFactors());
        } else {
            throw new DichotomyInternalException(String.format("Shift dispatcher configuration %s not handled currently", shiftDispatcherConfiguration.getClass().getName()));
        }
    }

    private static CseIdccShiftDispatcher getCseIdccDispatcher(CseIdccShiftDispatcherConfiguration configuration) {
        return new CseIdccShiftDispatcher(configuration.getSplittingFactors(), configuration.getReferenceExchanges(), configuration.getNtcs2());
    }

    private static IndexStrategy buildIndexStrategy(IndexStrategyConfiguration indexStrategyConfiguration) {
        if (indexStrategyConfiguration instanceof RangeDivisionIndexStrategyConfiguration) {
            RangeDivisionIndexStrategyConfiguration rangeDivisionIndexStrategyConfiguration = (RangeDivisionIndexStrategyConfiguration) indexStrategyConfiguration;
            return new RangeDivisionIndexStrategy(rangeDivisionIndexStrategyConfiguration.isStartWithMin());
        } else if (indexStrategyConfiguration instanceof StepsIndexStrategyConfiguration) {
            StepsIndexStrategyConfiguration stepsIndexStrategyConfiguration = (StepsIndexStrategyConfiguration) indexStrategyConfiguration;
            return new StepsIndexStrategy(stepsIndexStrategyConfiguration.isStartWithMin(), stepsIndexStrategyConfiguration.getStepsSize());
        } else {
            throw new DichotomyInternalException(String.format("Index strategy %s not handled currently", indexStrategyConfiguration.getClass().getName()));
        }
    }

    private Network loadNetwork(DichotomyFileResource network) {
        try (InputStream networkStream = urlValidationService.openUrlStream(network.getUrl())) {
            return Importers.loadNetwork(network.getFilename(), networkStream);
        } catch (IOException e) {
            throw new DichotomyInvalidDataException(String.format("Cannot download network file from URL '%s'", network.getUrl()), e);
        }
    }

    private ZonalData<Scalable> loadGlsk(DichotomyFileResource glsk, Network network) {
        try (InputStream glskStream = urlValidationService.openUrlStream(glsk.getUrl())) {
            return GlskDocumentImporters.importGlsk(glskStream).getZonalScalable(network);
        } catch (IOException e) {
            throw new DichotomyInvalidDataException(String.format("Cannot download GLSK file from URL '%s'", glsk.getUrl()), e);
        }
    }
}
