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

import org.eclipse.daanse.rdb.structure.api.model.Link;

public class LinkImpl implements Link {

    public ColumnImpl getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(ColumnImpl primaryKey) {
        this.primaryKey = primaryKey;
    }

    public ColumnImpl getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(ColumnImpl foreignKey) {
        this.foreignKey = foreignKey;
    }

    private ColumnImpl primaryKey;

    private ColumnImpl foreignKey;
}
