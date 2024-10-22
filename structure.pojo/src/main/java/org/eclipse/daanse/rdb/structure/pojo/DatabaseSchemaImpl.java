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

import org.eclipse.daanse.rdb.structure.api.model.DatabaseSchema;

public class DatabaseSchemaImpl implements DatabaseSchema {

    public DatabaseSchemaImpl(Builder builder) {
        this.tables = builder.tables;
        this.name = builder.name;
        this.id = builder.id;
    }

    public List<AbstractTable> getTables() {
        return tables;
    }

    public void setTables(List<AbstractTable> tables) {
        this.tables = tables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private List<AbstractTable> tables;

    private String name;

    private String id;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private List<AbstractTable> tables;

        private String name;

        private String id;

        public Builder withTables(List<AbstractTable> tables) {
            this.tables = tables;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public DatabaseSchemaImpl build() {
            return new DatabaseSchemaImpl(this);
        }

    }

}
