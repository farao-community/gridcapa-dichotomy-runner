/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.app;

import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInvalidDataException;
import com.farao_community.farao.dichotomy_runner.app.configuration.DichotomyServerProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.StringJoiner;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Component
public class UrlValidationService {
    private final DichotomyServerProperties.SecurityProperties securityProperties;

    public UrlValidationService(DichotomyServerProperties serverProperties) {
        this.securityProperties = serverProperties.getSecurity();
    }

    public InputStream openUrlStream(String urlString) throws IOException {
        if (securityProperties.getWhitelist().stream().noneMatch(urlString::startsWith)) {
            StringJoiner sj = new StringJoiner(", ", "Whitelist: ", ".");
            securityProperties.getWhitelist().forEach(sj::add);
            throw new DichotomyInvalidDataException(String.format("URL '%s' is not part of application's whitelist. %s", urlString, sj));
        }
        URL url = new URL(urlString);
        return url.openStream(); // NOSONAR Usage of whitelist not triggered by Sonar quality assessment, even if listed as a solution to the vulnerability
    }
}
