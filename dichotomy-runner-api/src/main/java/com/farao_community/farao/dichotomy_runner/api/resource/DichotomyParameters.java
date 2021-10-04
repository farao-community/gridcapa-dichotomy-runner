/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Optional;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DichotomyParameters {
    private static final IndexStrategyConfiguration DEFAULT_INDEX_STRATEGY_CONFIGURATION = new RangeDivisionIndexStrategyConfiguration(true);
    private final double minValue;
    private final double maxValue;
    private final double precision;
    private final ShiftDispatcherConfiguration shiftDispatcherConfiguration;
    private final IndexStrategyConfiguration indexStrategyConfiguration;

    @JsonCreator
    public DichotomyParameters(@JsonProperty("minValue") double minValue,
                               @JsonProperty("maxValue") double maxValue,
                               @JsonProperty("precision") double precision,
                               @JsonProperty("shiftDispatcherConfiguration") ShiftDispatcherConfiguration shiftDispatcherConfiguration,
                               @JsonProperty("indexStrategyConfiguration") IndexStrategyConfiguration indexStrategyConfiguration) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.precision = precision;
        this.shiftDispatcherConfiguration = shiftDispatcherConfiguration;
        this.indexStrategyConfiguration = Optional.ofNullable(indexStrategyConfiguration).orElse(DEFAULT_INDEX_STRATEGY_CONFIGURATION);
    }

    public DichotomyParameters(double minValue,
                               double maxValue,
                               double precision,
                               ShiftDispatcherConfiguration shiftDispatcherConfiguration) {
        this(minValue, maxValue, precision, shiftDispatcherConfiguration, DEFAULT_INDEX_STRATEGY_CONFIGURATION);
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getPrecision() {
        return precision;
    }

    public IndexStrategyConfiguration getIndexStrategyConfiguration() {
        return indexStrategyConfiguration;
    }

    public ShiftDispatcherConfiguration getShiftDispatcherConfiguration() {
        return shiftDispatcherConfiguration;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
