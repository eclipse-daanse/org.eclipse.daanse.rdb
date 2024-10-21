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

    @Override
    public List<? extends Row> getRows() {
        return rows;
    }

    public void setRows(List<? extends Row> rows) {
        this.rows = rows;
    }

}
