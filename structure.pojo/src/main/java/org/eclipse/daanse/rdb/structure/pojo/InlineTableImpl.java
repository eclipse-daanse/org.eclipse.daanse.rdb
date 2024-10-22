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
 *
 */
package org.eclipse.daanse.rdb.structure.pojo;

import java.util.List;

import org.eclipse.daanse.rdb.structure.api.model.InlineTable;
import org.eclipse.daanse.rdb.structure.api.model.Row;

public class InlineTableImpl extends AbstractTable implements InlineTable {

    private List<? extends Row> rows;

    public static Builder builder() {
        return new Builder();
    }

    public InlineTableImpl(Builder builder) {
        setName(builder.name);
        setColumns(builder.columns);
        setSchema(builder.schema);
        setDescription(builder.description);
        setRows(builder.rows);
    }

    @Override
    public List<? extends Row> getRows() {
        return rows;
    }

    public void setRows(List<? extends Row> rows) {
        this.rows = rows;
    }

    public static final class Builder {

        private String name;

        private List<ColumnImpl> columns;

        private DatabaseSchemaImpl schema;

        private String description;

        private List<? extends Row> rows;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withColumns(List<ColumnImpl> columns) {
            this.columns = columns;
            return this;
        }

        public Builder withsSchema(DatabaseSchemaImpl schema) {
            this.schema = schema;
            return this;
        }

        public Builder withsDdescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withRows(List<Row> rows) {
            this.rows = rows;
            return this;
        }

        public InlineTableImpl build() {
            return new InlineTableImpl(this);
        }

    }
}
