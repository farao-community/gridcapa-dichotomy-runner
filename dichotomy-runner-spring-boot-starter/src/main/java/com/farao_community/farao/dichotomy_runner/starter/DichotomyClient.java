/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.starter;

import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyRequest;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DichotomyClient {
    private static final int DEFAULT_PRIORITY = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(DichotomyClient.class);

    private final AmqpTemplate amqpTemplate;
    private final DichotomyClientProperties dichotomyClientProperties;
    private final DichotomyMessageHandler dichotomyMessageHandler;

    public DichotomyClient(AmqpTemplate amqpTemplate, DichotomyClientProperties dichotomyClientProperties) {
        this.amqpTemplate = amqpTemplate;
        this.dichotomyClientProperties = dichotomyClientProperties;
        this.dichotomyMessageHandler = new DichotomyMessageHandler(dichotomyClientProperties);
    }

    public DichotomyResponse runDichotomy(DichotomyRequest dichotomyRequest, int priority) {
        LOGGER.info("Dichotomy request sent: {}", dichotomyRequest);
        Message responseMessage = amqpTemplate.sendAndReceive(dichotomyClientProperties.getAmqp().getQueueName(), dichotomyMessageHandler.buildMessage(dichotomyRequest, priority));
        DichotomyResponse dichotomyResponse = dichotomyMessageHandler.readMessage(responseMessage);
        LOGGER.info("Dichotomy response received: {}", dichotomyResponse);
        return dichotomyResponse;
    }

    public DichotomyResponse runDichotomy(DichotomyRequest dichotomyRequest) {
        return runDichotomy(dichotomyRequest, DEFAULT_PRIORITY);
    }
}
