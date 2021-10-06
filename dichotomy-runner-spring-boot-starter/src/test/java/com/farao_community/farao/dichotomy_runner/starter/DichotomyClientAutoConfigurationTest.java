/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.dichotomy_runner.starter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.AsyncAmqpTemplate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class DichotomyClientAutoConfigurationTest {
    private AnnotationConfigApplicationContext context;

    @BeforeEach
    public void createContext() {
        context = new AnnotationConfigApplicationContext();
    }

    @AfterEach
    public void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void registersDichotomyClient() {
        context.registerBean("amqpTemplate", AmqpTemplate.class, () -> Mockito.mock(AmqpTemplate.class));
        context.register(DichotomyClientAutoConfiguration.class);
        context.refresh();
        DichotomyClient dichotomyClient = context.getBean(DichotomyClient.class);
        assertNotNull(dichotomyClient);
    }

    @Test
    void registersAsyncDichotomyClient() {
        context.registerBean("amqpTemplate", AmqpTemplate.class, () -> Mockito.mock(AmqpTemplate.class));
        context.registerBean("asyncAmqpTemplate", AsyncAmqpTemplate.class, () -> Mockito.mock(AsyncAmqpTemplate.class));
        context.register(DichotomyClientAutoConfiguration.class);
        context.refresh();
        AsyncDichotomyClient asyncDichotomyClient = context.getBean(AsyncDichotomyClient.class);
        assertNotNull(asyncDichotomyClient);
    }

}
