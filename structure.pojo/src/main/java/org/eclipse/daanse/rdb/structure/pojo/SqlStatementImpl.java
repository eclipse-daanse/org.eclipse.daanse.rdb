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

public class SqlStatementImpl implements SqlStatement {

    private List<String> dialects;
    private String sql;

    public SqlStatementImpl(Builder builder) {
        this.dialects = builder.dialects;
        this.sql = builder.sql;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> getDialects() {
        return dialects;
    }

    @Override
    public String getSql() {
        return sql;
    }

    public void setDialects(List<String> dialects) {
        this.dialects = dialects;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public static final class Builder {

        private List<String> dialects;
        private String sql;

        public Builder withDialects(List<String> dialects) {
            this.dialects = dialects;
            return this;
        }

        public Builder withSql(String sql) {
            this.sql = sql;
            return this;
        }

        public SqlStatementImpl build() {
            return new SqlStatementImpl(this);
        }
    }
}
