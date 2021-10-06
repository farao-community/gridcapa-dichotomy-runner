/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.app;

import com.farao_community.farao.dichotomy.network.scaling.CseIdccShiftDispatcher;
import com.farao_community.farao.dichotomy.network.scaling.ShiftDispatcher;
import com.farao_community.farao.dichotomy.network.scaling.SplittingFactors;
import com.farao_community.farao.dichotomy_runner.api.resource.*;
import com.farao_community.farao.rao_runner.api.resource.RaoResponse;
import com.farao_community.farao.rao_runner.starter.RaoRunnerClient;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import static com.farao_community.farao.dichotomy_runner.app.DichotomyHandler.buildShiftDispatcher;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@SpringBootTest
class DichotomyHandlerTest {
    private static final double EPSILON = 1e-3;
    private static final RaoResponse SECURED_RAO_RESPONSE = new RaoResponse(
            "id",
            "instant",
            "file:///fakeUrl/fake.txt",
            DichotomyHandlerTest.class.getResource("/testCrac.json").toExternalForm(),
            DichotomyHandlerTest.class.getResource("/securedRaoResult.json").toExternalForm()
    );
    private static final RaoResponse UNSECURED_RAO_RESPONSE = new RaoResponse(
            "id",
            "instant",
            "file:///fakeUrl/fake.txt",
            DichotomyHandlerTest.class.getResource("/testCrac.json").toExternalForm(),
            DichotomyHandlerTest.class.getResource("/unsecuredRaoResult.json").toExternalForm()
    );

    @MockBean
    private MinioAdapter minioAdapter;

    @MockBean
    private RaoRunnerClient raoRunnerClient;

    @Autowired
    private DichotomyHandler dichotomyHandler;

    @Test
    void checkDichotomyHandlerReturnsOnlyHigherSecureWhenRaoResultOk() {
        String requestId = "Test request";
        DichotomyFileResource networkFile = createFileResource(getClass().getResource("/testNetwork.uct"));
        DichotomyFileResource cracFile = createFileResource(getClass().getResource("/testCrac.json"));
        DichotomyFileResource glskFile = createFileResource(getClass().getResource("/testGlsk.xml"));
        DichotomyFileResource raoParametersFile = createFileResource(getClass().getResource("/raoParametersWithAdnLoadflow.json"));
        ShiftDispatcherConfiguration splittingFactors = new SplittingFactorsConfiguration(ImmutableMap.of(
                "10YFR-RTE------C", 0.25,
                "10YCH-SWISSGRIDZ", 0.1,
                "10YAT-APG------L", 0.4,
                "10YSI-ELES-----O", 0.25,
                "10YIT-GRTN-----B", -1.0
        ));
        DichotomyParameters dichotomyParameters = new DichotomyParameters(
                -2000.,
                0.,
                50.,
                splittingFactors,
                new RangeDivisionIndexStrategyConfiguration(false)
        );
        DichotomyRequest dichotomyRequest = new DichotomyRequest(requestId, networkFile, cracFile, glskFile, raoParametersFile, dichotomyParameters);

        // WHEN
        Mockito.when(raoRunnerClient.runRao(Mockito.any())).thenReturn(SECURED_RAO_RESPONSE);
        Mockito.when(minioAdapter.generateFileResource(Mockito.anyString())).thenReturn(new DichotomyFileResource("fake.txt", "file:///fakeUrl/fake.txt"));

        DichotomyResponse response = dichotomyHandler.handleDichotomyRequest(dichotomyRequest);

        assertNotNull(response.getHigherSecureStep());
        assertNull(response.getLowerUnsecureStep());
    }

    @Test
    void checkDichotomyHandlerReturnsOnlyLowerUnsecureWhenRaoResultKoKo() {
        String requestId = "Test request";
        DichotomyFileResource networkFile = createFileResource(getClass().getResource("/testNetwork.uct"));
        DichotomyFileResource cracFile = createFileResource(getClass().getResource("/testCrac.json"));
        DichotomyFileResource glskFile = createFileResource(getClass().getResource("/testGlsk.xml"));
        DichotomyFileResource raoParametersFile = createFileResource(getClass().getResource("/raoParametersWithAdnLoadflow.json"));
        ShiftDispatcherConfiguration splittingFactors = new SplittingFactorsConfiguration(ImmutableMap.of(
                "10YFR-RTE------C", 0.25,
                "10YCH-SWISSGRIDZ", 0.1,
                "10YAT-APG------L", 0.4,
                "10YSI-ELES-----O", 0.25,
                "10YIT-GRTN-----B", -1.0
        ));
        DichotomyParameters dichotomyParameters = new DichotomyParameters(
                -2000.,
                0.,
                50.,
                splittingFactors,
                new RangeDivisionIndexStrategyConfiguration(false)
        );
        DichotomyRequest dichotomyRequest = new DichotomyRequest(requestId, networkFile, cracFile, glskFile, raoParametersFile, dichotomyParameters);

        // WHEN
        Mockito.when(raoRunnerClient.runRao(Mockito.any())).thenReturn(UNSECURED_RAO_RESPONSE);
        Mockito.when(minioAdapter.generateFileResource(Mockito.anyString())).thenReturn(new DichotomyFileResource("fake.txt", "file:///fakeUrl/fake.txt"));

        DichotomyResponse response = dichotomyHandler.handleDichotomyRequest(dichotomyRequest);

        assertNull(response.getHigherSecureStep());
        assertNotNull(response.getLowerUnsecureStep());
    }

    @Test
    void checkDichotomyHandlerReturnsBothWhenRaoResultKoOk() {

        String requestId = "Test request";
        DichotomyFileResource networkFile = createFileResource(getClass().getResource("/testNetwork.uct"));
        DichotomyFileResource cracFile = createFileResource(getClass().getResource("/testCrac.json"));
        DichotomyFileResource glskFile = createFileResource(getClass().getResource("/testGlsk.xml"));
        DichotomyFileResource raoParametersFile = createFileResource(getClass().getResource("/raoParametersWithAdnLoadflow.json"));
        ShiftDispatcherConfiguration splittingFactors = new SplittingFactorsConfiguration(ImmutableMap.of(
                "10YFR-RTE------C", 0.25,
                "10YCH-SWISSGRIDZ", 0.1,
                "10YAT-APG------L", 0.4,
                "10YSI-ELES-----O", 0.25,
                "10YIT-GRTN-----B", -1.0
        ));
        DichotomyParameters dichotomyParameters = new DichotomyParameters(
                -2000.,
                0.,
                1500.,
                splittingFactors,
                new RangeDivisionIndexStrategyConfiguration(false)
        );
        DichotomyRequest dichotomyRequest = new DichotomyRequest(requestId, networkFile, cracFile, glskFile, raoParametersFile, dichotomyParameters);

        // WHEN

        Mockito.when(raoRunnerClient.runRao(Mockito.any())).thenReturn(UNSECURED_RAO_RESPONSE, SECURED_RAO_RESPONSE);
        Mockito.when(minioAdapter.generateFileResource(Mockito.anyString())).thenReturn(new DichotomyFileResource("fake.txt", "file:///fakeUrl/fake.txt"));

        DichotomyResponse response = dichotomyHandler.handleDichotomyRequest(dichotomyRequest);

        assertNotNull(response.getHigherSecureStep());
        assertNotNull(response.getLowerUnsecureStep());
        assertEquals(-1000., response.getHigherSecureStep().getStepValue(), EPSILON);
        assertEquals(0., response.getLowerUnsecureStep().getStepValue(), EPSILON);
    }

    @Test
    void checkDichotomyHandlerReturnsBothWhenRaoResultKoOkWithStepsStrategy() {

        String requestId = "Test request";
        DichotomyFileResource networkFile = createFileResource(getClass().getResource("/testNetwork.uct"));
        DichotomyFileResource cracFile = createFileResource(getClass().getResource("/testCrac.json"));
        DichotomyFileResource glskFile = createFileResource(getClass().getResource("/testGlsk.xml"));
        DichotomyFileResource raoParametersFile = createFileResource(getClass().getResource("/raoParametersWithAdnLoadflow.json"));
        ShiftDispatcherConfiguration splittingFactors = new SplittingFactorsConfiguration(ImmutableMap.of(
                "10YFR-RTE------C", 0.25,
                "10YCH-SWISSGRIDZ", 0.1,
                "10YAT-APG------L", 0.4,
                "10YSI-ELES-----O", 0.25,
                "10YIT-GRTN-----B", -1.0
        ));
        DichotomyParameters dichotomyParameters = new DichotomyParameters(
                -2000.,
                0.,
                450.,
                splittingFactors,
                new StepsIndexStrategyConfiguration(false, 500.)
        );
        DichotomyRequest dichotomyRequest = new DichotomyRequest(requestId, networkFile, cracFile, glskFile, raoParametersFile, dichotomyParameters);

        // WHEN

        Mockito.when(raoRunnerClient.runRao(Mockito.any())).thenReturn(UNSECURED_RAO_RESPONSE, UNSECURED_RAO_RESPONSE, SECURED_RAO_RESPONSE, UNSECURED_RAO_RESPONSE);
        Mockito.when(minioAdapter.generateFileResource(Mockito.anyString())).thenReturn(new DichotomyFileResource("fake.txt", "file:///fakeUrl/fake.txt"));

        DichotomyResponse response = dichotomyHandler.handleDichotomyRequest(dichotomyRequest);

        assertNotNull(response.getHigherSecureStep());
        assertNotNull(response.getLowerUnsecureStep());
        assertEquals(-1000., response.getHigherSecureStep().getStepValue(), EPSILON);
        assertEquals(-750., response.getLowerUnsecureStep().getStepValue(), EPSILON);
    }

    @Test
    void checkDichotomyHandlerReturnsBothWhenRaoResultKoOkWithStepsStrategyAndCseIdccShiftDispatcher() {
        String requestId = "Test request";
        DichotomyFileResource networkFile = createFileResource(getClass().getResource("/testNetwork.uct"));
        DichotomyFileResource cracFile = createFileResource(getClass().getResource("/testCrac.json"));
        DichotomyFileResource glskFile = createFileResource(getClass().getResource("/testGlsk.xml"));
        DichotomyFileResource raoParametersFile = createFileResource(getClass().getResource("/raoParametersWithAdnLoadflow.json"));

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

        DichotomyParameters dichotomyParameters = new DichotomyParameters(
                -2000.,
                0.,
                450.,
                new CseIdccShiftDispatcherConfiguration(splittingFactors, referenceExchanges, ntcs2),
                new StepsIndexStrategyConfiguration(false, 500.)
        );
        DichotomyRequest dichotomyRequest = new DichotomyRequest(requestId, networkFile, cracFile, glskFile, raoParametersFile, dichotomyParameters);

        // WHEN
        Mockito.when(raoRunnerClient.runRao(Mockito.any())).thenReturn(UNSECURED_RAO_RESPONSE, UNSECURED_RAO_RESPONSE, SECURED_RAO_RESPONSE, UNSECURED_RAO_RESPONSE);
        Mockito.when(minioAdapter.generateFileResource(Mockito.anyString())).thenReturn(new DichotomyFileResource("fake.txt", "file:///fakeUrl/fake.txt"));

        DichotomyResponse response = dichotomyHandler.handleDichotomyRequest(dichotomyRequest);

        assertNotNull(response.getHigherSecureStep());
        assertNotNull(response.getLowerUnsecureStep());
        assertEquals(-1000., response.getHigherSecureStep().getStepValue(), EPSILON);
        assertEquals(-750., response.getLowerUnsecureStep().getStepValue(), EPSILON);
    }

    @Test
    void testBuildShiftDispatcherWithSplittingFactors() {
        ShiftDispatcherConfiguration splittingFactors = new SplittingFactorsConfiguration(ImmutableMap.of(
                "10YFR-RTE------C", 0.25,
                "10YCH-SWISSGRIDZ", 0.1,
                "10YAT-APG------L", 0.4,
                "10YSI-ELES-----O", 0.25,
                "10YIT-GRTN-----B", -1.0
        ));

        ShiftDispatcher shiftDispatcher = buildShiftDispatcher(splittingFactors);
        assertTrue(shiftDispatcher instanceof SplittingFactors);
    }

    @Test
    void testBuildShiftDispatcherWithCseIdccShiftDispatcher() {
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

        ShiftDispatcher shiftDispatcher = buildShiftDispatcher(new CseIdccShiftDispatcherConfiguration(splittingFactors, referenceExchanges, ntcs2));
        assertTrue(shiftDispatcher instanceof CseIdccShiftDispatcher);
    }

    private DichotomyFileResource createFileResource(URL resource) {
        return new DichotomyFileResource(resource.getFile(), resource.toExternalForm());
    }
}
