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

import org.eclipse.daanse.rdb.structure.api.model.SqlStatement;
import org.eclipse.daanse.rdb.structure.api.model.SqlView;

public class SqlViewImpl extends AbstractTable implements SqlView {

    private List<? extends SqlStatement> sqlStatements;

    public SqlViewImpl(Builder builder) {
        setName(builder.getName());
        setColumns(builder.getColumns());
        setSchema(builder.getSchema());
        setDescription(builder.getDescription());
        setSqlStatements(builder.sqlStatements);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<? extends SqlStatement> getSqlStatements() {
        return sqlStatements;
    }

    public void setSqlStatements(List<? extends SqlStatement> sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

    public static final class Builder extends AbstractBuilder {

        private List<? extends SqlStatement> sqlStatements;

        private Builder() {
        }

        public Builder withSqlStatements(List<? extends SqlStatement> sqlStatements) {
            this.sqlStatements = sqlStatements;
            return this;
        }

        public SqlViewImpl build() {
            return new SqlViewImpl(this);
        }

    }
}
