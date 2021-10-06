/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.app;

import com.farao_community.farao.dichotomy_runner.api.JsonConverter;
import com.farao_community.farao.dichotomy_runner.api.exception.AbstractDichotomyException;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInternalException;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyRequest;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyResponse;
import com.farao_community.farao.dichotomy_runner.app.configuration.AmqpMessagesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Component;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Component
public class DichotomyListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DichotomyListener.class);
    private static final String APPLICATION_ID = "dichotomy-server";
    private static final String CONTENT_ENCODING = "UTF-8";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private static final int PRIORITY = 1;

    private final JsonConverter jsonConverter;
    private final DichotomyHandler dichotomyHandler;
    private final AmqpTemplate amqpTemplate;
    private final AmqpMessagesConfiguration amqpMessagesConfiguration;

    public DichotomyListener(DichotomyHandler dichotomyHandler, AmqpTemplate amqpTemplate, AmqpMessagesConfiguration amqpMessagesConfiguration) {
        this.jsonConverter = new JsonConverter();
        this.dichotomyHandler = dichotomyHandler;
        this.amqpTemplate = amqpTemplate;
        this.amqpMessagesConfiguration = amqpMessagesConfiguration;
    }

    @Override
    public void onMessage(Message message) {
        String replyTo = message.getMessageProperties().getReplyTo();
        String correlationId = message.getMessageProperties().getCorrelationId();
        try {
            DichotomyRequest dichotomyRequest = jsonConverter.fromJsonMessage(message.getBody(), DichotomyRequest.class);
            LOGGER.info("Dichotomy request received: {}", dichotomyRequest);
            DichotomyResponse dichotomyResponse = dichotomyHandler.handleDichotomyRequest(dichotomyRequest);
            LOGGER.info("Dichotomy response sent: {}", dichotomyResponse);
            sendDichotomyResponse(dichotomyResponse, replyTo, correlationId);
        } catch (AbstractDichotomyException e) {
            LOGGER.error("Dichotomy exception occured", e);
            sendErrorResponse(e, replyTo, correlationId);
        } catch (Exception e) {
            LOGGER.error("Unknown exception occured", e);
            AbstractDichotomyException wrappingException = new DichotomyInternalException("Unknown exception", e);
            sendErrorResponse(wrappingException, replyTo, correlationId);
        }
    }

    private void sendDichotomyResponse(DichotomyResponse dichotomyResponse, String replyTo, String correlationId) {
        if (replyTo != null) {
            amqpTemplate.send(replyTo, createMessageResponse(dichotomyResponse, correlationId));
        } else {
            amqpTemplate.send(amqpMessagesConfiguration.dichotomyResponseExchange().getName(), "", createMessageResponse(dichotomyResponse, correlationId));
        }
    }

    private void sendErrorResponse(AbstractDichotomyException exception, String replyTo, String correlationId) {
        if (replyTo != null) {
            amqpTemplate.send(replyTo, createErrorResponse(exception, correlationId));
        } else {
            amqpTemplate.send(amqpMessagesConfiguration.dichotomyResponseExchange().getName(), "", createErrorResponse(exception, correlationId));
        }
    }

    private Message createMessageResponse(DichotomyResponse dichotomyResponse, String correlationId) {
        return MessageBuilder.withBody(jsonConverter.toJsonMessage(dichotomyResponse))
                .andProperties(buildMessageResponseProperties(correlationId))
                .build();
    }

    private Message createErrorResponse(AbstractDichotomyException exception, String correlationId) {
        return MessageBuilder.withBody(jsonConverter.toJsonMessage(exception))
                .andProperties(buildMessageResponseProperties(correlationId))
                .build();
    }

    private MessageProperties buildMessageResponseProperties(String correlationId) {
        return MessagePropertiesBuilder.newInstance()
                .setAppId(APPLICATION_ID)
                .setContentEncoding(CONTENT_ENCODING)
                .setContentType(CONTENT_TYPE)
                .setCorrelationId(correlationId)
                .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                .setExpiration(amqpMessagesConfiguration.dichotomyResponseExpiration())
                .setPriority(PRIORITY)
                .build();
    }
}
