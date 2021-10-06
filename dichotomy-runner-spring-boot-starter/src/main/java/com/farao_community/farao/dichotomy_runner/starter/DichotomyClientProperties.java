/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.dichotomy_runner.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@ConfigurationProperties("dichotomy-server")
public class DichotomyClientProperties {
    private AmqpConfiguration amqp;

    public AmqpConfiguration getAmqp() {
        return amqp;
    }

    public void setAmqp(AmqpConfiguration amqp) {
        this.amqp = amqp;
    }

    public static class AmqpConfiguration {
        private String queueName;
        private String expiration;
        private String applicationId;

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public String getExpiration() {
            return expiration;
        }

        public void setExpiration(String expiration) {
            this.expiration = expiration;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }
    }
}
