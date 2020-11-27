/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.audit.generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.audit.util.NamingUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.logging.log4j.audit.generator.Constants.PUBLIC;

/**
 * Generates the Classes and Interfaces for Audit Logging based on data in the Catalog.
 */
public final class ClassGenerator {
    private static final Logger LOGGER = LogManager.getLogger(ClassGenerator.class);

    protected final List<AccessorDefinition> beanMethods = new ArrayList<>();
    private boolean isClass = true;
    private final String className;
    private String parentClassName;
    private String packageName;
    private final String baseFolder;
    private String javadocComment;
    private boolean verbose;
    private final List<String> implementsDeclarations = new ArrayList<>();

    private final Set<String> importsDeclarations = new HashSet<>();

    private final List<VariableDefinition> localVariables = new ArrayList<>();

    private final List<ConstructorDefinition> constructors = new ArrayList<>();

    private final List<MethodDefinition> methods = new ArrayList<>();

    private boolean runPrewrite = false;

    private boolean isAbstract = false;

    private String visability = PUBLIC;

    private String annotations = null;

    private String code = null;

    private String typeStatement = null;

    public ClassGenerator(String className, String baseFolder) {
        this.className = className;
        this.baseFolder = baseFolder;
    }

    public String getTypeStatement() {
        return typeStatement;
    }

    public void setTypeStatement(String typeStatement) {
        this.typeStatement = typeStatement;
    }

    /**
     * Code is not resolved and is just injected straight into the main code
     * block of the respective class
     *
     * @return
     */

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void addBeanMethods(AccessorDefinition beanDefinition) {
        beanMethods.add(beanDefinition);
    }

    public void addConstructor(ConstructorDefinition constructorDefinition) {
        constructors.add(constructorDefinition);
    }

    public void addLocalVariable(VariableDefinition definition) {
        localVariables.add(definition);
    }

    public void addMethod(MethodDefinition definition) {
        methods.add(definition);
    }

    public void addSingelton(String name, List<String> parameters) {
        if (Character.isUpperCase(name.charAt(0))) {
            name = name.substring(0, 1).toLowerCase() + name.substring(1);
        }

        VariableDefinition definition = new VariableDefinition("private",
                getClassName(), name, null);
        definition.setMakeStatic(true);
        addLocalVariable(definition);
        addMethod(MethodDefinition.getStandardSingleton(getClassName(), name, parameters));
    }

    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public List<AccessorDefinition> getBeanMethods() {
        return beanMethods;
    }

    public String getClassName() {
        return className;
    }

    public String getParentClassName() {
        return parentClassName;
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }

    public List<String> getImplements() {
        return implementsDeclarations;
    }

    public Set<String> getImports() {
        return importsDeclarations;
    }

    public List<MethodDefinition> getMethodDefinitions() {
        return methods;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<VariableDefinition> getVariableDefinitions() {
        return localVariables;
    }

    public String getVisability() {
        return visability;
    }

    public void setVisability(String visability) {
        this.visability = visability;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public boolean isClass() {
        return isClass;
    }

    public void setClass(boolean isClass) {
        this.isClass = isClass;
    }

    /**
     * Override this method it gets called once before toString do if toString
     * gets called 5 time this will only be called on the first
     */
    public void preWrite() {

    }

    public void generate() throws Exception {
        StringBuilder sb = new StringBuilder(baseFolder);
        if (getPackageName() != null) {
            sb.append("/").append(getPackageName().replaceAll("\\.", "/"));
        }
        sb.append("/").append(NamingUtils.upperFirst(getClassName()))
                .append(".java");
        String fullPath = sb.toString();
        if (verbose) {
            LOGGER.info(fullPath);
        }
        File file = new File(fullPath);
        try (DataOutputStream out = new DataOutputStream(openOutputStream(file))) {
            out.writeBytes(getClassContents());
        }
    }

public java.lang.String getClassContents() throws java.lang.Exception {
    if (getClassName() == null) {
        throw new java.lang.Exception("Class name has to be set");
    }
    if (!runPrewrite) {
        preWrite();
        runPrewrite = true;
    }
    java.lang.StringBuilder sb = new java.lang.StringBuilder();
    sb.append("package ").append(getPackageName()).append(";\n\n");
    if (!getImports().isEmpty()) {
        java.util.List<java.lang.String> list = new java.util.ArrayList<>(getImports());
        java.util.Collections.sort(list);
        for (java.lang.String element : list) {
            sb.append("import ").append(element).append(";\n");
        }
        sb.append("\n");
    }
    sb.append("/**\n");
    {
        sb.append(" * ").append(/* NPEX_NULL_EXP */
        getJavadocComment());
    }
    sb.append("\n * @author generated");
    sb.append("\n */\n");
    if (annotations != null) {
        sb.append(annotations);
        sb.append("\n");
    }
    sb.append(getVisability());
    if (isClass()) {
        sb.append(" class ");
    } else {
        sb.append(" interface ");
    }
    sb.append(getClassName());
    if (typeStatement != null) {
        sb.append(" <").append(typeStatement).append("> ");
    }
    if ((getParentClassName() != null) && (getParentClassName().length() > 0)) {
        sb.append(" extends ").append(getParentClassName());
    }
    if (!getImplements().isEmpty()) {
        sb.append(" implements ");
        boolean first = true;
        for (java.lang.String element : getImplements()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(element);
            first = false;
        }
    }
    sb.append(" {\n\n");
    if (!localVariables.isEmpty()) {
        java.util.Collections.sort(localVariables);
        for (org.apache.logging.log4j.audit.generator.VariableDefinition element : localVariables) {
            sb.append(element).append("\n");
        }
    }
    if (!constructors.isEmpty()) {
        java.util.Collections.sort(constructors);
        for (org.apache.logging.log4j.audit.generator.ConstructorDefinition element : constructors) {
            sb.append(element).append("\n\n");
        }
    }
    if ((beanMethods.size() > 0) && isClass()) {
        org.apache.logging.log4j.audit.generator.MethodDefinition definition = new org.apache.logging.log4j.audit.generator.MethodDefinition("String", "toString");
        java.lang.StringBuilder buffer = new java.lang.StringBuilder();
        buffer.append("\tStringBuilder sb = new StringBuilder();");
        buffer.append("\n\tsb.append(super.toString());");
        for (org.apache.logging.log4j.audit.generator.AccessorDefinition element : beanMethods) {
            buffer.append("\n\tsb.append(\", ");
            buffer.append(element.getName()).append("=\").append(").append(org.apache.logging.log4j.audit.util.NamingUtils.getAccessorName(element.getName(), element.getType())).append("());");
        }
        buffer.append("\n\treturn sb.toString();");
        definition.setContent(buffer.toString());
        methods.add(definition);
    }
    if (methods != null) {
        java.util.Collections.sort(methods);
        for (org.apache.logging.log4j.audit.generator.MethodDefinition element : methods) {
            sb.append(element).append("\n\n");
        }
    }
    if (code != null) {
        sb.append(code).append("\n");
    }
    sb.append("}");
    return sb.toString();
}

    public String getJavadocComment() {
        return javadocComment;
    }

    public void setJavadocComment(String javadocComment) {
        this.javadocComment = javadocComment;
    }

    private OutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            final File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, false);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
