/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.serverless.workflow.utils;

import java.util.Optional;

import org.kie.kogito.jackson.utils.JsonObjectUtils;
import org.kie.kogito.jackson.utils.MergeUtils;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ExpressionHandlerUtils {

    private ExpressionHandlerUtils() {
    }

    public static void assign(ObjectNode context, JsonNode target, JsonNode value, String expr) {
        if (target == null || target.isNull()) {
            target = value.isArray() ? ObjectMapperFactory.get().createArrayNode() : ObjectMapperFactory.get().createObjectNode();
        }
        Optional<String> varName = fallbackVarToName(expr);
        if (varName.isPresent()) {
            JsonObjectUtils.addToNode(varName.get(), MergeUtils.merge(value, target), context);
        }
    }

    public static Optional<String> fallbackVarToName(String expr) {
        int indexOf = expr.lastIndexOf('.');
        return indexOf < 0 ? Optional.empty() : Optional.of(expr.substring(indexOf + 1));
    }
}