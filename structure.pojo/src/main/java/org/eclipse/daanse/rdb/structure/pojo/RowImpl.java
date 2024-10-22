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

import org.eclipse.daanse.rdb.structure.api.model.Row;
import org.eclipse.daanse.rdb.structure.api.model.RowValue;

public class RowImpl implements Row {

    private List<? extends RowValue> rowValues;

    public RowImpl(Builder builder) {
        this.rowValues = builder.rowValues;
    }

    @Override
    public List<? extends RowValue> getRowValues() {
        return rowValues;
    }

    public void setRowValues(List<? extends RowValue> rowValues) {
        this.rowValues = rowValues;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private List<? extends RowValue> rowValues;

        public Builder withRowValues(List<? extends RowValue> rowValues) {
            this.rowValues = rowValues;
            return this;
        }

        public RowImpl build() {
            return new RowImpl(this);
        }
    }
}
