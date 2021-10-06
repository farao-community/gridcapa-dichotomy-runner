/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api;

import com.farao_community.farao.dichotomy_runner.api.exception.AbstractDichotomyException;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInternalException;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInvalidDataException;
import com.farao_community.farao.dichotomy_runner.api.resource.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class JsonConverterTest {
    private static final double EPSILON = 1e-3;

    @Test
    void checkDichotomyInputsJsonConversion() throws URISyntaxException, IOException {
        JsonConverter jsonConverter = new JsonConverter();
        String inputMessage = Files.readString(Paths.get(getClass().getResource("/DichotomyStartMessage.json").toURI()));
        DichotomyRequest dichotomyRequest = jsonConverter.fromJsonMessage(inputMessage.getBytes(), DichotomyRequest.class);
        assertEquals("id", dichotomyRequest.getId());
        assertEquals("network.txt", dichotomyRequest.getNetwork().getFilename());
        assertEquals("https://network/file/url", dichotomyRequest.getNetwork().getUrl());
        assertEquals("crac.txt", dichotomyRequest.getCrac().getFilename());
        assertEquals("https://crac/file/url", dichotomyRequest.getCrac().getUrl());
        assertEquals("glsk.txt", dichotomyRequest.getGlsk().getFilename());
        assertEquals("https://glsk/file/url", dichotomyRequest.getGlsk().getUrl());
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

    @Test
    void checkExceptionThrownWhenInvalidJson() throws URISyntaxException, IOException {
        JsonConverter jsonConverter = new JsonConverter();
        String inputMessage = Files.readString(Paths.get(getClass().getResource("/DichotomyStartInvalidMessage.json").toURI()));
        byte[] messageBytes = inputMessage.getBytes();
        Assertions.assertThrows(DichotomyInvalidDataException.class, () -> jsonConverter.fromJsonMessage(messageBytes, DichotomyRequest.class));
    }

    @Test
    void checkInternalExceptionJsonConversion() throws URISyntaxException, IOException {
        JsonConverter jsonConverter = new JsonConverter();
        AbstractDichotomyException exception = new DichotomyInternalException("Something really bad happened");
        String expectedMessage = Files.readString(Paths.get(getClass().getResource("/DichotomyInternalError.json").toURI()));
        assertEquals(expectedMessage, new String(jsonConverter.toJsonMessage(exception)));
    }

    @Test
    void checkDichotomyInputsJsonConversionWithRangeDivisionIndexStrategy() throws URISyntaxException, IOException {
        JsonConverter jsonConverter = new JsonConverter();
        String inputMessage = Files.readString(Paths.get(getClass().getResource("/DichotomyStartMessageRangeDivisionStrategy.json").toURI()));
        DichotomyRequest dichotomyRequest = jsonConverter.fromJsonMessage(inputMessage.getBytes(), DichotomyRequest.class);
        assertTrue(dichotomyRequest.getParameters().getIndexStrategyConfiguration() instanceof RangeDivisionIndexStrategyConfiguration);
        RangeDivisionIndexStrategyConfiguration indexStrategyConfiguration = (RangeDivisionIndexStrategyConfiguration) dichotomyRequest.getParameters().getIndexStrategyConfiguration();
        assertTrue(indexStrategyConfiguration.isStartWithMin());
    }

    @Test
    void checkDichotomyInputsJsonConversionWithStepsIndexStrategy() throws URISyntaxException, IOException {
        JsonConverter jsonConverter = new JsonConverter();
        String inputMessage = Files.readString(Paths.get(getClass().getResource("/DichotomyStartMessageStepsStrategy.json").toURI()));
        DichotomyRequest dichotomyRequest = jsonConverter.fromJsonMessage(inputMessage.getBytes(), DichotomyRequest.class);
        assertTrue(dichotomyRequest.getParameters().getIndexStrategyConfiguration() instanceof StepsIndexStrategyConfiguration);
        StepsIndexStrategyConfiguration indexStrategyConfiguration = (StepsIndexStrategyConfiguration) dichotomyRequest.getParameters().getIndexStrategyConfiguration();
        assertFalse(indexStrategyConfiguration.isStartWithMin());
        assertEquals(500., indexStrategyConfiguration.getStepsSize(), EPSILON);
    }

    @Test
    void checkDefaultValueForIndexStrategyConfiguration() throws URISyntaxException, IOException {
        JsonConverter jsonConverter = new JsonConverter();
        String inputMessage = Files.readString(Paths.get(getClass().getResource("/DichotomyStartMessage.json").toURI()));
        DichotomyRequest dichotomyRequest = jsonConverter.fromJsonMessage(inputMessage.getBytes(), DichotomyRequest.class);
        assertTrue(dichotomyRequest.getParameters().getShiftDispatcherConfiguration() instanceof SplittingFactorsConfiguration);
        assertTrue(dichotomyRequest.getParameters().getIndexStrategyConfiguration() instanceof RangeDivisionIndexStrategyConfiguration);
        RangeDivisionIndexStrategyConfiguration indexStrategyConfiguration = (RangeDivisionIndexStrategyConfiguration) dichotomyRequest.getParameters().getIndexStrategyConfiguration();
        assertTrue(indexStrategyConfiguration.isStartWithMin());
    }

    @Test
    void checkDichotomyInputsJsonConversionWithCseIdccShiftDispatcher() throws URISyntaxException, IOException {
        JsonConverter jsonConverter = new JsonConverter();
        String inputMessage = Files.readString(Paths.get(getClass().getResource("/DichotomyStartMessageCseIdccShiftDispatcher.json").toURI()));
        DichotomyRequest dichotomyRequest = jsonConverter.fromJsonMessage(inputMessage.getBytes(), DichotomyRequest.class);
        assertTrue(dichotomyRequest.getParameters().getShiftDispatcherConfiguration() instanceof CseIdccShiftDispatcherConfiguration);
        CseIdccShiftDispatcherConfiguration configuration = (CseIdccShiftDispatcherConfiguration) dichotomyRequest.getParameters().getShiftDispatcherConfiguration();
        assertEquals(0.4, configuration.getSplittingFactors().get("10YFR-RTE------C"), 0.01);
    }
}
