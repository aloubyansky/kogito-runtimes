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

package org.kie.kogito.codegen.api.di.impl;

import org.kie.kogito.codegen.api.di.DependencyInjectionAnnotator;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

public class CDIDependencyInjectionAnnotator implements DependencyInjectionAnnotator {

    @Override
    public <T extends NodeWithAnnotations<?>> T withProduces(T node, boolean isDefault) {
        node.addAndGetAnnotation("javax.enterprise.inject.Produces");
        if (isDefault) {
            node.addAndGetAnnotation("io.quarkus.arc.DefaultBean");
        }
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withNamed(T node, String name) {
        node.addAnnotation(new SingleMemberAnnotationExpr(new Name("javax.inject.Named"), new StringLiteralExpr(name)));
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withApplicationComponent(T node) {
        node.addAnnotation("javax.enterprise.context.ApplicationScoped");
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withNamedApplicationComponent(T node, String name) {
        return withNamed(withApplicationComponent(node), name);
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withSingletonComponent(T node) {
        node.addAnnotation("javax.inject.Singleton");
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withNamedSingletonComponent(T node, String name) {
        return withNamed(withSingletonComponent(node), name);
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withInjection(T node, boolean lazy) {
        node.addAnnotation("javax.inject.Inject");
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withNamedInjection(T node, String name) {
        return withNamed(withInjection(node), name);
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withOptionalInjection(T node) {
        return withInjection(node);
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withIncomingMessage(T node, String channel) {
        node.addAnnotation(new SingleMemberAnnotationExpr(new Name("org.eclipse.microprofile.reactive.messaging.Incoming"), new StringLiteralExpr(channel)));
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withOutgoingMessage(T node, String channel) {
        node.addAnnotation(new SingleMemberAnnotationExpr(new Name("org.eclipse.microprofile.reactive.messaging.Channel"), new StringLiteralExpr(channel)));
        return node;
    }

    @Override
    public MethodCallExpr withMessageProducer(MethodCallExpr produceMethod, String channel, Expression event) {
        produceMethod.addArgument(event);
        return produceMethod;
    }

    @Override
    public String optionalInstanceInjectionType() {
        return "javax.enterprise.inject.Instance";
    }

    @Override
    public Expression optionalInstanceExists(String fieldName) {
        MethodCallExpr condition = new MethodCallExpr(new NameExpr(fieldName), "isUnsatisfied");
        return new BinaryExpr(condition, new BooleanLiteralExpr(false), BinaryExpr.Operator.EQUALS);
    }

    @Override
    public String multiInstanceInjectionType() {
        return optionalInstanceInjectionType();
    }

    @Override
    public Expression getMultiInstance(String fieldName) {
        return new MethodCallExpr(
                new MethodCallExpr(new NameExpr("java.util.stream.StreamSupport"), "stream", NodeList.nodeList(
                        new MethodCallExpr(new NameExpr(fieldName), "spliterator"),
                        new BooleanLiteralExpr(false))),
                "collect",
                NodeList.nodeList(
                        new MethodCallExpr(new NameExpr("java.util.stream.Collectors"), "toList")));
    }

    @Override
    public String applicationComponentType() {
        return "javax.enterprise.context.ApplicationScoped";
    }

    @Override
    public String emitterType(String dataType) {
        return "org.eclipse.microprofile.reactive.messaging.Emitter<" + dataType + ">";
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withConfigInjection(T node, String configKey) {
        node.addAnnotation(new NormalAnnotationExpr(
                new Name("org.eclipse.microprofile.config.inject.ConfigProperty"),
                NodeList.nodeList(
                        new MemberValuePair("name", new StringLiteralExpr(configKey)))));
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withConfigInjection(T node, String configKey, String defaultValue) {
        node.addAnnotation(new NormalAnnotationExpr(
                new Name("org.eclipse.microprofile.config.inject.ConfigProperty"),
                NodeList.nodeList(
                        new MemberValuePair("name", new StringLiteralExpr(configKey)),
                        new MemberValuePair("defaultValue", new StringLiteralExpr(defaultValue)))));
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withEagerStartup(T node) {
        node.addAnnotation("io.quarkus.runtime.Startup");
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withFactoryClass(T node) {
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withFactoryMethod(T node) {
        node.addAnnotation("javax.enterprise.inject.Produces");
        return node;
    }

    @Override
    public <T extends NodeWithAnnotations<?>> T withTagAnnotation(T node, NodeList<MemberValuePair> attributes) {
        node.addAnnotation(new NormalAnnotationExpr(new Name("org.eclipse.microprofile.openapi.annotations.tags.Tag"), attributes));
        return node;
    }
}
