/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.rdb.structure.pojo;

import java.util.List;

import org.eclipse.daanse.rdb.structure.api.model.Column;
import org.eclipse.daanse.rdb.structure.api.model.Table;

public class ColumnImpl implements Column {

    public ColumnImpl(Builder builder) {
        this.name = builder.name;
        this.table = builder.table;
        this.type = builder.type;
        this.typeQualifiers = builder.typeQualifiers;
        this.description = builder.description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTypeQualifiers() {
        return typeQualifiers;
    }

    public void setTypeQualifiers(List<String> typeQualifiers) {
        this.typeQualifiers = typeQualifiers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String name;

    private Table table;

    private String type;

    private List<String> typeQualifiers;

    private String description;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String name;

        private Table table;

        private String type;

        private List<String> typeQualifiers;

        private String description;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTable(Table table) {
            this.table = table;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withTypeQualifiers(List<String> typeQualifiers) {
            this.typeQualifiers = typeQualifiers;
            return this;
        }

        public Builder withTypeQualifiers(String description) {
            this.description = description;
            return this;
        }

        public ColumnImpl build() {
            return new ColumnImpl(this);
        }
    }

}
