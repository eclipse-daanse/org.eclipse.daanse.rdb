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

import org.eclipse.daanse.rdb.structure.api.model.ViewTable;
import org.eclipse.daanse.rdb.structure.pojo.InlineTableImpl.Builder;

public class ViewTableImpl extends AbstractTable implements ViewTable {

    public ViewTableImpl(Builder builder) {
        setName(builder.name);
        setColumns(builder.columns);
        setSchema(builder.schema);
        setDescription(builder.description);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder {

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

        public ViewTableImpl build() {
            return new ViewTableImpl(this);
        }
    }
}
