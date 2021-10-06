/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api.resource;

import com.farao_community.farao.dichotomy.network.scaling.CseCountry;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInvalidDataException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.farao_community.farao.dichotomy.network.scaling.CseCountry.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class CseIdccShiftDispatcherConfiguration extends SplittingFactorsConfiguration implements ShiftDispatcherConfiguration {
    private final Map<String, Double> referenceExchanges;
    private final Map<String, Double> ntcs2;

    @JsonCreator
    public CseIdccShiftDispatcherConfiguration(@JsonProperty("splittingFactors") Map<String, Double> splittingFactors,
                                               @JsonProperty("referenceExchanges") Map<String, Double> referenceExchanges,
                                               @JsonProperty("ntcs2") Map<String, Double> ntcs2) {
        super(splittingFactors);
        checkExchangesData(referenceExchanges);
        checkExchangesData(ntcs2);
        this.referenceExchanges = referenceExchanges;
        this.ntcs2 = ntcs2;
    }

    @Override
    protected void checkSplittingFactors(Map<String, Double> splittingFactors) {
        super.checkSplittingFactors(splittingFactors);
        if (!splittingFactors.keySet().equals(Stream.of(CseCountry.values()).map(CseCountry::getEiCode).collect(Collectors.toSet()))) {
            throw new DichotomyInvalidDataException(
                    String.format("Exchanges values (ref or ntc2) must be listed for the following countries : " +
                    "FR (%s), CH (%s), AT (%s), SI (%s), IT (%s).", FR, CH, AT, SI, IT));
        }
    }

    private void checkExchangesData(Map<String, Double> exchanges) {
        Set<String> italianBorderEiCodes = Set.of(FR, SI, CH, AT).stream()
                .map(CseCountry::getEiCode)
                .collect(Collectors.toSet());
        if (!exchanges.keySet().equals(italianBorderEiCodes)) {
            throw new DichotomyInvalidDataException(
                    String.format("Exchanges values (ref or ntc2) must be listed for the following countries : " +
                    "FR (%s), CH (%s), AT (%s), SI (%s).", FR, CH, AT, SI));
        }
    }

    public Map<String, Double> getReferenceExchanges() {
        return referenceExchanges;
    }

    public Map<String, Double> getNtcs2() {
        return ntcs2;
    }
}
