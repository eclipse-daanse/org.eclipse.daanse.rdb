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

import org.eclipse.daanse.rdb.structure.api.model.Table;

public abstract class AbstractTable implements Table {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ColumnImpl> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnImpl> columns) {
        this.columns = columns;
    }

    public DatabaseSchemaImpl getSchema() {
        return schema;
    }

    public void setSchema(DatabaseSchemaImpl schema) {
        this.schema = schema;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String name;

    private List<ColumnImpl> columns;

    private DatabaseSchemaImpl schema;

    private String description;
}
