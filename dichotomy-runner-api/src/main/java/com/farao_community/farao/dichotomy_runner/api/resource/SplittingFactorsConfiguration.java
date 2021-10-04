/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api.resource;

import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInvalidDataException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class SplittingFactorsConfiguration implements ShiftDispatcherConfiguration {
    private static final double DOUBLE_TOLERANCE = 0.001;
    protected final Map<String, Double> splittingFactors;

    @JsonCreator
    public SplittingFactorsConfiguration(@JsonProperty("splittingFactors") Map<String, Double> splittingFactors) {
        checkSplittingFactors(splittingFactors);
        this.splittingFactors = splittingFactors;
    }

    protected void checkSplittingFactors(Map<String, Double> splittingFactors) {
        if (Math.abs(splittingFactors.values().stream().reduce(0., Double::sum)) >= DOUBLE_TOLERANCE) {
            throw new DichotomyInvalidDataException("Splitting factors sum must be equal to 0.");
        }
    }

    public Map<String, Double> getSplittingFactors() {
        return splittingFactors;
    }
}
