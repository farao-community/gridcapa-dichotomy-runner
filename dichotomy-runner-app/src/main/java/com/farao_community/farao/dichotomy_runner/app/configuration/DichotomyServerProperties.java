/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.app.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Collections;
import java.util.List;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@ConstructorBinding
@ConfigurationProperties("dichotomy-server")
public class DichotomyServerProperties {
    private final RequestsProperties requests;
    private final ResponsesProperties responses;
    private final MinioProperties minio;
    private final SecurityProperties security;

    public DichotomyServerProperties(RequestsProperties requests,
                                     ResponsesProperties responses,
                                     MinioProperties minio,
                                     SecurityProperties security) {
        this.requests = requests;
        this.responses = responses;
        this.minio = minio;
        this.security = security;
    }

    public RequestsProperties getRequests() {
        return requests;
    }

    public ResponsesProperties getResponses() {
        return responses;
    }

    public MinioProperties getMinio() {
        return minio;
    }

    public SecurityProperties getSecurity() {
        return security;
    }

    public static class RequestsProperties {
        private final String queueName;

        public RequestsProperties(String queueName) {
            this.queueName = queueName;
        }

        public String getQueueName() {
            return queueName;
        }
    }

    public  static class ResponsesProperties {
        private final String exchange;
        private final String expiration;

        public ResponsesProperties(String exchange, String expiration) {
            this.exchange = exchange;
            this.expiration = expiration;
        }

        public String getExchange() {
            return exchange;
        }

        public String getExpiration() {
            return expiration;
        }
    }

    public static class MinioProperties {
        private final String url;
        private final String bucket;
        private final String basePath;
        private final Access access;

        public MinioProperties(String url, String bucket, String basePath, Access access) {
            this.url = url;
            this.bucket = bucket;
            this.basePath = basePath;
            this.access = access;
        }

        public String getUrl() {
            return url;
        }

        public String getBucket() {
            return bucket;
        }

        public String getBasePath() {
            return basePath;
        }

        public Access getAccess() {
            return access;
        }

        public static class Access {
            private final String name;
            private final String secret;

            public Access(String name, String secret) {
                this.name = name;
                this.secret = secret;
            }

            public String getName() {
                return name;
            }

            public String getSecret() {
                return secret;
            }
        }
    }

    public static class SecurityProperties {
        private final List<String> whitelist;

        public SecurityProperties(List<String> whitelist) {
            this.whitelist = Collections.unmodifiableList(whitelist);
        }

        public List<String> getWhitelist() {
            return whitelist;
        }
    }
}
