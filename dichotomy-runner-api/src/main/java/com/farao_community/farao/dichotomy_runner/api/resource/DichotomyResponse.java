/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Type("dichotomy-response")
public class DichotomyResponse {
    @Id
    private final String id;
    private final DichotomyStepResponse highestValidStep;
    private final DichotomyStepResponse lowestInvalidStep;
    private final LimitingCause limitingCause;
    private final String limitingFailureMessage;

    @JsonCreator
    public DichotomyResponse(@JsonProperty("id") String id,
                             @JsonProperty("highestValidStep") DichotomyStepResponse highestValidStep,
                             @JsonProperty("lowestInvalidStep") DichotomyStepResponse lowestInvalidStep,
                             @JsonProperty("limitingCause") LimitingCause limitingCause,
                             @JsonProperty("limitingFailureMessage") String limitingFailureMessage) {
        this.id = id;
        this.highestValidStep = highestValidStep;
        this.lowestInvalidStep = lowestInvalidStep;
        this.limitingCause = limitingCause;
        this.limitingFailureMessage = limitingFailureMessage;
    }

    public DichotomyResponse(String id,
                             DichotomyStepResponse highestValidStep,
                             DichotomyStepResponse lowerInvalidStep,
                             LimitingCause limitingCause) {
        this.id = id;
        this.highestValidStep = highestValidStep;
        this.lowestInvalidStep = lowerInvalidStep;
        this.limitingCause = limitingCause;
        this.limitingFailureMessage = "None";
    }

    public String getId() {
        return id;
    }

    public DichotomyStepResponse getHighestValidStep() {
        return highestValidStep;
    }

    public DichotomyStepResponse getLowestInvalidStep() {
        return lowestInvalidStep;
    }

    public LimitingCause getLimitingCause() {
        return limitingCause;
    }

    public String getLimitingFailureMessage() {
        return limitingFailureMessage;
    }

    public boolean hasValidStep() {
        return highestValidStep != null;
    }

    public double getHighestValidStepValue() {
        return highestValidStep != null ? highestValidStep.getStepValue() : Double.NaN;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
