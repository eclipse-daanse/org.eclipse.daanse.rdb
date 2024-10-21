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

import org.eclipse.daanse.rdb.structure.api.model.Column;
import org.eclipse.daanse.rdb.structure.api.model.RowValue;

public class RowValueImpl implements RowValue {

    private Column column;
    private String value;

    @Override
    public Column getColumn() {
        return column;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
