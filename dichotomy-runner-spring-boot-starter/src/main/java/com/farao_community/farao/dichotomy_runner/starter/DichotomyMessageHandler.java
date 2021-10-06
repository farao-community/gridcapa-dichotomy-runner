/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.dichotomy_runner.starter;

import com.farao_community.farao.dichotomy_runner.api.JsonConverter;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInternalException;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyRequest;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyResponse;
import org.springframework.amqp.core.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DichotomyMessageHandler {
    private static final String CONTENT_ENCODING = "UTF-8";
    private static final String CONTENT_TYPE = "application/vnd.api+json";

    private final DichotomyClientProperties clientProperties;
    private final JsonConverter jsonConverter;

    public DichotomyMessageHandler(DichotomyClientProperties clientProperties) {
        this.clientProperties = clientProperties;
        this.jsonConverter = new JsonConverter();
    }

    public Message buildMessage(DichotomyRequest dichotomyRequest, int priority) {
        return MessageBuilder.withBody(jsonConverter.toJsonMessage(dichotomyRequest))
                .andProperties(buildMessageProperties(priority))
                .build();
    }

    private MessageProperties buildMessageProperties(int priority) {
        return MessagePropertiesBuilder.newInstance()
                .setAppId(clientProperties.getAmqp().getApplicationId())
                .setContentEncoding(CONTENT_ENCODING)
                .setContentType(CONTENT_TYPE)
                .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                .setExpiration(clientProperties.getAmqp().getExpiration())
                .setPriority(priority)
                .build();
    }

    public DichotomyResponse readMessage(Message message) {
        if (message != null) {
            return jsonConverter.fromJsonMessage(message.getBody(), DichotomyResponse.class);
        } else {
            throw new DichotomyInternalException("Dichotomy server did not respond");
        }
    }
}
