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

import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;

public class DatabaseCatalogImpl implements DatabaseCatalog {

    private List<DatabaseSchemaImpl> schemas;

    public DatabaseCatalogImpl(Builder builder) {
        this.schemas = builder.schemas;
        this.links = builder.links;
    }

    public List<DatabaseSchemaImpl> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<DatabaseSchemaImpl> schemas) {
        this.schemas = schemas;
    }

    public List<LinkImpl> getLinks() {
        return links;
    }

    public void setLinks(List<LinkImpl> links) {
        this.links = links;
    }

    private List<LinkImpl> links;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private List<DatabaseSchemaImpl> schemas;
        private List<LinkImpl> links;

        public Builder withSchemas(List<DatabaseSchemaImpl> schemas) {
            this.schemas = schemas;
            return this;
        }

        public Builder withLinks(List<LinkImpl> links) {
            this.links = links;
            return this;
        }

        public DatabaseCatalogImpl build() {
            return new DatabaseCatalogImpl(this);
        }
    }

}
