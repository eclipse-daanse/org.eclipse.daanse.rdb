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

package org.eclipse.daanse.rdb.guard.jsqltranspiler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.daanse.rdb.guard.api.SqlGuard;
import org.eclipse.daanse.rdb.guard.api.SqlGuardFactory;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.pojo.ColumnImpl;
import org.eclipse.daanse.rdb.structure.pojo.DatabaseCatalogImpl;
import org.eclipse.daanse.rdb.structure.pojo.DatabaseSchemaImpl;
import org.eclipse.daanse.rdb.structure.pojo.PhysicalTableImpl;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

public class Test1 {

    @Test
    void testName(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colBar = ColumnImpl.builder().withName("bar").build();
        ColumnImpl colBuzz = ColumnImpl.builder().withName("buzz").build();
        PhysicalTableImpl table = PhysicalTableImpl.builder().withName("foo").withColumns(List.of(colBar, colBuzz))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName("sch").withTables(List.of(table))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", "sch", databaseCatalog);

        String result = guard.guard("select * from foo");

        assertThat(result)
                .isEqualTo("SELECT sch.foo.bar /* Resolved Column*/ , sch.foo.buzz /* Resolved Column*/  FROM sch.foo");
    }
}
