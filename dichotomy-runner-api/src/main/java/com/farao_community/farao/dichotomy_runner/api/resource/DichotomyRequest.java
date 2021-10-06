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

import java.util.Objects;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Type("dichotomy-request")
public class DichotomyRequest {

    @Id
    private final String id;
    private final DichotomyFileResource network;
    private final DichotomyFileResource crac;
    private final DichotomyFileResource glsk;
    private final DichotomyFileResource raoParameters;
    private final DichotomyParameters parameters;

    @JsonCreator
    public DichotomyRequest(@JsonProperty("id") String id,
                            @JsonProperty("network") DichotomyFileResource network,
                            @JsonProperty("crac") DichotomyFileResource crac,
                            @JsonProperty("glsk") DichotomyFileResource glsk,
                            @JsonProperty("raoParameters") DichotomyFileResource raoParameters,
                            @JsonProperty("parameters") DichotomyParameters parameters) {
        this.id = id;
        this.network = Objects.requireNonNull(network);
        this.crac = Objects.requireNonNull(crac);
        this.glsk = Objects.requireNonNull(glsk);
        this.raoParameters = Objects.requireNonNull(raoParameters);
        this.parameters = Objects.requireNonNull(parameters);
    }

    public String getId() {
        return id;
    }

    public DichotomyFileResource getNetwork() {
        return network;
    }

    public DichotomyFileResource getCrac() {
        return crac;
    }

    public DichotomyFileResource getGlsk() {
        return glsk;
    }

    public DichotomyFileResource getRaoParameters() {
        return raoParameters;
    }

    public DichotomyParameters getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
