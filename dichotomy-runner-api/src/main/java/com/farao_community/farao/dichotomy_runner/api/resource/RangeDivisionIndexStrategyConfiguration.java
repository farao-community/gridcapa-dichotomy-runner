/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class RangeDivisionIndexStrategyConfiguration implements IndexStrategyConfiguration {
    private final boolean startWithMin;

    @JsonCreator
    public RangeDivisionIndexStrategyConfiguration(@JsonProperty("startWithMin") boolean startWithMin) {
        this.startWithMin = startWithMin;
    }

    public boolean isStartWithMin() {
        return startWithMin;
    }
}
