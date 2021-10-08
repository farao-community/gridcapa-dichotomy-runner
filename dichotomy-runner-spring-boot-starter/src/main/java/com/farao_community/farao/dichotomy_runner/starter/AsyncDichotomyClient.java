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
import org.springframework.amqp.core.AsyncAmqpTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class AsyncDichotomyClient {
    private static final int DEFAULT_PRIORITY = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncDichotomyClient.class);

    private final AsyncAmqpTemplate asyncAmqpTemplate;
    private final DichotomyClientProperties dichotomyClientProperties;
    private final DichotomyMessageHandler dichotomyMessageHandler;

    public AsyncDichotomyClient(AsyncAmqpTemplate asyncAmqpTemplate, DichotomyClientProperties dichotomyClientProperties) {
        this.asyncAmqpTemplate = asyncAmqpTemplate;
        this.dichotomyClientProperties = dichotomyClientProperties;
        this.dichotomyMessageHandler = new DichotomyMessageHandler(dichotomyClientProperties);
    }

    public CompletableFuture<DichotomyResponse> runDichotomy(DichotomyRequest dichotomyRequest, int priority) {
        LOGGER.info("Dichotomy request sent: {}", dichotomyRequest);
        return asyncAmqpTemplate.sendAndReceive(
                dichotomyClientProperties.getAmqp().getQueueName(),
                dichotomyMessageHandler.buildMessage(dichotomyRequest, priority))
                    .completable().thenApply(message -> {
                        DichotomyResponse dichotomyResponse = dichotomyMessageHandler.readMessage(message);
                        LOGGER.info("Dichotomy response received: {}", dichotomyResponse);
                        return dichotomyResponse;
                    });
    }

    public CompletableFuture<DichotomyResponse> runDichotomy(DichotomyRequest dichotomyRequest) {
        return runDichotomy(dichotomyRequest, DEFAULT_PRIORITY);
    }
}
