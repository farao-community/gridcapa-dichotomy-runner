/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.starter;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.AsyncAmqpTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Configuration
@EnableConfigurationProperties(DichotomyClientProperties.class)
public class DichotomyClientAutoConfiguration {
    private final DichotomyClientProperties clientProperties;

    public DichotomyClientAutoConfiguration(DichotomyClientProperties clientProperties) {
        this.clientProperties = clientProperties;
    }

    @Bean
    @ConditionalOnBean(AsyncAmqpTemplate.class)
    public AsyncDichotomyClient asyncDichotomyClient(AsyncAmqpTemplate asyncAmqpTemplate) {
        return new AsyncDichotomyClient(asyncAmqpTemplate, clientProperties);
    }

    @Bean
    public DichotomyClient dichotomyClient(AmqpTemplate amqpTemplate) {
        return new DichotomyClient(amqpTemplate, clientProperties);
    }
}
