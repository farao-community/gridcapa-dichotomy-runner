/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy_runner.api;

import com.farao_community.farao.dichotomy_runner.api.exception.AbstractDichotomyException;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInternalException;
import com.farao_community.farao.dichotomy_runner.api.exception.DichotomyInvalidDataException;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyRequest;
import com.farao_community.farao.dichotomy_runner.api.resource.DichotomyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.SerializationFeature;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.github.jasminb.jsonapi.models.errors.Error;

/**
 * JSON API conversion component
 * Allows automatic conversion from resources or exceptions towards JSON API formatted bytes.
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class JsonConverter {
    private final ObjectMapper objectMapper;

    public JsonConverter() {
        this.objectMapper = createObjectMapper();
    }

    public <T> T fromJsonMessage(byte[] jsonMessage, Class<T> tClass) {
        ResourceConverter converter = createConverter();
        try {
            return converter.readDocument(jsonMessage, tClass).get();
        } catch (Exception e) {
            throw new DichotomyInvalidDataException(String.format("Message cannot be converted to class %s", tClass.getName()), e);
        }
    }

    public <T> byte[] toJsonMessage(T jsonApiObject) {
        ResourceConverter converter = createConverter();
        JSONAPIDocument<?> jsonapiDocument = new JSONAPIDocument<>(jsonApiObject);
        try {
            return converter.writeDocument(jsonapiDocument);
        } catch (DocumentSerializationException e) {
            throw new DichotomyInternalException("Exception occurred during object conversion", e);
        }
    }

    public byte[] toJsonMessage(AbstractDichotomyException exception) {
        ResourceConverter converter = createConverter();
        JSONAPIDocument<?> jsonapiDocument = new JSONAPIDocument<>(convertExceptionToJsonError(exception));
        try {
            return converter.writeDocument(jsonapiDocument);
        } catch (DocumentSerializationException e) {
            throw new DichotomyInternalException("Exception occurred during exception message conversion", e);
        }
    }

    private ResourceConverter createConverter() {
        ResourceConverter converter = new ResourceConverter(objectMapper, DichotomyRequest.class, DichotomyResponse.class);
        converter.disableSerializationOption(SerializationFeature.INCLUDE_META);
        return converter;
    }

    private Error convertExceptionToJsonError(AbstractDichotomyException exception) {
        Error error = new Error();
        error.setStatus(Integer.toString(exception.getStatus()));
        error.setCode(exception.getCode());
        error.setTitle(exception.getTitle());
        error.setDetail(exception.getDetails());
        return error;
    }

    private ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}
