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

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DichotomyStepResponse {
    private final double stepValue;
    private final DichotomyFileResource networkWithPra;
    private final DichotomyFileResource cracResult;
    private final DichotomyFileResource raoResult;

    @JsonCreator
    public DichotomyStepResponse(@JsonProperty("stepValue") double stepValue,
                                 @JsonProperty("networkWithPra") DichotomyFileResource networkWithPra,
                                 @JsonProperty("cracResult") DichotomyFileResource cracResult,
                                 @JsonProperty("raoResult") DichotomyFileResource raoResult) {
        this.stepValue = stepValue;
        this.networkWithPra = networkWithPra;
        this.cracResult = cracResult;
        this.raoResult = raoResult;
    }

    public double getStepValue() {
        return stepValue;
    }

    public DichotomyFileResource getNetworkWithPra() {
        return networkWithPra;
    }

    public DichotomyFileResource getCracResult() {
        return cracResult;
    }

    public DichotomyFileResource getRaoResult() {
        return raoResult;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
