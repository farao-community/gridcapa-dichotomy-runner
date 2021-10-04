/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api.resource;

import com.farao_community.farao.dichotomy.network.ReasonUnsecure;
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
    private final DichotomyStepResponse higherSecureStep;
    private final DichotomyStepResponse lowerUnsecureStep;
    private final ReasonUnsecure reasonUnsecure;
    private final String extraInformations;

    @JsonCreator
    public DichotomyResponse(@JsonProperty("id") String id,
                             @JsonProperty("higherSecureStep") DichotomyStepResponse higherSecureStep,
                             @JsonProperty("lowerUnsecureStep") DichotomyStepResponse lowerUnsecureStep,
                             @JsonProperty("reasonUnsecure") ReasonUnsecure reasonUnsecure,
                             @JsonProperty("extraInformations") String extraInformations) {
        this.id = id;
        this.higherSecureStep = higherSecureStep;
        this.lowerUnsecureStep = lowerUnsecureStep;
        this.reasonUnsecure = reasonUnsecure;
        this.extraInformations = extraInformations;
    }

    public DichotomyResponse(String id,
                             DichotomyStepResponse higherSecureStep,
                             DichotomyStepResponse lowerUnsecureStep,
                             ReasonUnsecure reasonUnsecure) {
        this.id = id;
        this.higherSecureStep = higherSecureStep;
        this.lowerUnsecureStep = lowerUnsecureStep;
        this.reasonUnsecure = reasonUnsecure;
        this.extraInformations = "none";
    }

    public String getId() {
        return id;
    }

    public DichotomyStepResponse getHigherSecureStep() {
        return higherSecureStep;
    }

    public DichotomyStepResponse getLowerUnsecureStep() {
        return lowerUnsecureStep;
    }

    public ReasonUnsecure getReasonUnsecure() {
        return reasonUnsecure;
    }

    public String getExtraInformations() {
        return extraInformations;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
