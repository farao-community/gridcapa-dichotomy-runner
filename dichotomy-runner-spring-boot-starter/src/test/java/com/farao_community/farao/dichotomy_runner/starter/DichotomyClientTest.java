/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.dichotomy_runner.starter;

import com.farao_community.farao.dichotomy_runner.api.JsonConverter;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyRequest;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class DichotomyClientTest {
    private static final double EPSILON = 1e-3;

    private final JsonConverter jsonConverter = new JsonConverter();

    @Test
    void checkThatDichotomyClientHandlesMessagesCorrectly() throws IOException {
        AmqpTemplate amqpTemplate = Mockito.mock(AmqpTemplate.class);
        DichotomyClient client = new DichotomyClient(amqpTemplate, buildProperties());
        DichotomyRequest dichotomyRequest = jsonConverter.fromJsonMessage(getClass().getResourceAsStream("/DichotomyRequestMessage.json").readAllBytes(), DichotomyRequest.class);

        Message responseMessage = Mockito.mock(Message.class);
        Mockito.when(responseMessage.getBody()).thenReturn(getClass().getResourceAsStream("/DichotomyResponseMessage.json").readAllBytes());
        Mockito.when(amqpTemplate.sendAndReceive(Mockito.same("my-queue"), Mockito.any())).thenReturn(responseMessage);

        DichotomyResponse dichotomyResponse = client.runDichotomy(dichotomyRequest);

        assertEquals(1000., dichotomyResponse.getHigherSecureStep().getStepValue(), EPSILON);
        assertEquals(1500., dichotomyResponse.getLowerUnsecureStep().getStepValue(), EPSILON);
    }

    private DichotomyClientProperties buildProperties() {
        DichotomyClientProperties properties = new DichotomyClientProperties();
        DichotomyClientProperties.AmqpConfiguration amqpConfiguration = new DichotomyClientProperties.AmqpConfiguration();
        amqpConfiguration.setQueueName("my-queue");
        amqpConfiguration.setExpiration("10000");
        amqpConfiguration.setApplicationId("application-id");
        properties.setAmqp(amqpConfiguration);
        return properties;
    }
}
